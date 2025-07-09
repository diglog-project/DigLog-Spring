package api.store.diglog.service.post;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.testcontainers.shaded.org.awaitility.Awaitility.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.test.context.ActiveProfiles;

import api.store.diglog.model.constant.Platform;
import api.store.diglog.model.constant.Role;
import api.store.diglog.model.entity.Folder;
import api.store.diglog.model.entity.Member;
import api.store.diglog.model.entity.Post;
import api.store.diglog.repository.FolderRepository;
import api.store.diglog.repository.MemberRepository;
import api.store.diglog.repository.PostRepository;
import api.store.diglog.supporter.RedisTestSupporter;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class PostAsyncWorkerTest extends RedisTestSupporter {

	@Autowired
	private PostAsyncWorker postAsyncWorker;
	@Autowired
	private PostRepository postRepository;
	@Autowired
	private MemberRepository memberRepository;
	@Autowired
	private FolderRepository folderRepository;
	@Autowired
	private RedisTemplate<String, String> redisTemplate;

	private static final String COUNT_PREFIX = "post:view:count:";
	private static final String DIRTY_SET = "post:view:dirtySet";

	private List<Member> members;
	private List<Folder> folders;

	@BeforeEach
	void setUp() {

		members = memberRepository.saveAll(
			IntStream.range(0, 5)
				.mapToObj(i -> Member.builder()
					.email("Frod" + i + "@gmail.com")
					.username("Frod" + i)
					.password("FrodPassword" + i)
					.roles(Set.of(Role.ROLE_USER))
					.platform(Platform.SERVER)
					.createdAt(LocalDateTime.of(2022, 2 + i, 22, 12, 0))
					.updatedAt(LocalDateTime.of(2022, 3 + i, 22, 12, 0))
					.build())
				.toList()
		);

		folders = folderRepository.saveAll(
			IntStream.range(0, 5)
				.mapToObj(i -> Folder.builder()
					.id(UUID.randomUUID())
					.member(members.get(i))
					.title("diglog" + i)
					.depth(0)
					.orderIndex(0)
					.parentFolder(null)
					.build())
				.toList()
		);

	}

	@AfterEach
	void tearDown() {
		postRepository.deleteAllInBatch();
		folderRepository.deleteAllInBatch();
		memberRepository.deleteAllInBatch();
		redisTemplate.getConnectionFactory().getConnection().serverCommands().flushAll();
	}

	@DisplayName("레디스의 조회수를 DB에 업데이트 후 DirtySet을 초기화한다.")
	@Test
	void syncViewCountAllInBatch() {

		// given
		List<Post> posts = postRepository.saveAll(
			IntStream.range(0, 5)
				.mapToObj(i -> postRepository.save(Post.builder()
					.title("title" + i)
					.content("content" + i)
					.viewCount(i)
					.member(members.get(i))
					.folder(folders.get(i))
					.build()))
				.toList()
		);

		Map<UUID, Long> updateViewCount = posts.stream()
			.collect(Collectors.toMap(
				Post::getId,
				post -> post.getViewCount() * 2
			));

		for (UUID postId : updateViewCount.keySet()) {
			redisTemplate.opsForValue().set(COUNT_PREFIX + postId, String.valueOf(updateViewCount.get(postId)));
			redisTemplate.opsForSet().add(DIRTY_SET, postId.toString());
		}

		List<UUID> postIds = posts.stream()
			.map(Post::getId)
			.toList();

		// when
		postAsyncWorker.syncViewCountAllInBatch(postIds);

		// then
		await().atMost(10, TimeUnit.SECONDS)
			.untilAsserted(() ->
				assertAll(
					() -> {
						List<Post> updatedPosts = postRepository.findAllById(postIds);

						assertThat(updatedPosts)
							.extracting("id", "viewCount")
							.containsExactlyInAnyOrderElementsOf(
								updateViewCount.keySet().stream()
									.map(postId -> tuple(postId, updateViewCount.get(postId)))
									.toList()
							);
					},
					() -> assertThat(redisTemplate.opsForSet().members(DIRTY_SET))
						.isEmpty()
				)
			);
	}

	@DisplayName("레디스에 존재하지 않는 조회수는 동기화되지 않는다.")
	@Test
	void syncViewCountAllInBatch_shouldIgnoreNull() {
		// given
		List<Post> posts = postRepository.saveAll(
			IntStream.range(0, 5)
				.mapToObj(i -> postRepository.save(Post.builder()
					.title("title" + i)
					.content("content" + i)
					.viewCount(i)
					.member(members.get(i))
					.folder(folders.get(i))
					.build()))
				.toList()
		);

		Map<UUID, Long> updateViewCount = posts.stream()
			.collect(Collectors.toMap(
				Post::getId,
				post -> post.getViewCount() * 2
			));

		for (UUID postId : updateViewCount.keySet()) {
			redisTemplate.opsForValue().set(COUNT_PREFIX + postId, String.valueOf(updateViewCount.get(postId)));
			redisTemplate.opsForSet().add(DIRTY_SET, postId.toString());
		}

		List<UUID> postIds = new ArrayList<>(posts.stream()
			.map(Post::getId)
			.toList());

		redisTemplate.delete(COUNT_PREFIX + postIds.getLast().toString());

		// when
		postAsyncWorker.syncViewCountAllInBatch(postIds);

		// then
		UUID notUpdatedPostId = postIds.removeLast();

		await().atMost(10, TimeUnit.SECONDS)
			.untilAsserted(() ->
				assertAll(
					() -> {
						List<Post> updatedPosts = postRepository.findAllById(postIds);

						assertThat(updatedPosts)
							.extracting("id", "viewCount")
							.containsExactlyInAnyOrderElementsOf(
								updateViewCount.keySet().stream()
									.filter(id -> id != notUpdatedPostId)
									.map(postId -> tuple(postId, updateViewCount.get(postId)))
									.toList()
							);
					},

					() -> {
						Post post = postRepository.findById(notUpdatedPostId)
							.orElseThrow(() -> new IllegalArgumentException("존재하지 않는 게시글"));
						assertThat(post.getViewCount()).isEqualTo(4);
					},

					() -> assertThat(redisTemplate.opsForSet().members(DIRTY_SET))
						.doesNotContainAnyElementsOf(
							postIds.stream()
								.map(UUID::toString)
								.toList()
						)
						.contains(notUpdatedPostId.toString())
				)
			);
	}

	@DisplayName("레디스에 조회수가 숫자가 아니면 동기화되지 않는다.")
	@Test
	void syncViewCountAllInBatch_shouldIgnoreInvalidValues() {
		// given
		List<Post> posts = postRepository.saveAll(
			IntStream.range(0, 5)
				.mapToObj(i -> postRepository.save(Post.builder()
					.title("title" + i)
					.content("content" + i)
					.viewCount(i)
					.member(members.get(i))
					.folder(folders.get(i))
					.build()))
				.toList()
		);

		Map<UUID, Long> updateViewCount = posts.stream()
			.collect(Collectors.toMap(
				Post::getId,
				post -> post.getViewCount() * 2
			));

		for (UUID postId : updateViewCount.keySet()) {
			redisTemplate.opsForValue().set(COUNT_PREFIX + postId, String.valueOf(updateViewCount.get(postId)));
			redisTemplate.opsForSet().add(DIRTY_SET, postId.toString());
		}

		List<UUID> postIds = new ArrayList<>(posts.stream()
			.map(Post::getId)
			.toList());

		redisTemplate.opsForValue().set(COUNT_PREFIX + postIds.getLast().toString(), "not number");

		// when
		postAsyncWorker.syncViewCountAllInBatch(postIds);

		// then
		UUID notUpdatedPostId = postIds.removeLast();

		await().atMost(15, TimeUnit.SECONDS)
			.untilAsserted(() ->
				assertAll(
					() -> {
						List<Post> updatedPosts = postRepository.findAllById(postIds);

						assertThat(updatedPosts)
							.extracting("id", "viewCount")
							.containsExactlyInAnyOrderElementsOf(
								updateViewCount.keySet().stream()
									.filter(id -> id != notUpdatedPostId)
									.map(postId -> tuple(postId, updateViewCount.get(postId)))
									.toList()
							);
					},

					() -> {
						Post post = postRepository.findById(notUpdatedPostId)
							.orElseThrow(() -> new IllegalArgumentException("존재하지 않는 게시글"));
						assertThat(post.getViewCount()).isEqualTo(4);
					},

					() -> assertThat(redisTemplate.opsForSet().members(DIRTY_SET))
						.doesNotContainAnyElementsOf(
							postIds.stream()
								.map(UUID::toString)
								.toList()
						)
						.contains(notUpdatedPostId.toString())
				)
			);
	}

	@DisplayName("레디스의 조회수가 DB보다 작으면 동기화되지 않는다.")
	@Test
	void syncViewCountAllInBatch_shouldIgnoreRedisViewCountIsLessThanDb() {
		// given
		List<Post> posts = postRepository.saveAll(
			IntStream.range(0, 5)
				.mapToObj(i -> postRepository.save(Post.builder()
					.title("title" + i)
					.content("content" + i)
					.viewCount(i)
					.member(members.get(i))
					.folder(folders.get(i))
					.build()))
				.toList()
		);

		Map<UUID, Long> updateViewCount = posts.stream()
			.collect(Collectors.toMap(
				Post::getId,
				post -> post.getViewCount() * 2
			));

		for (UUID postId : updateViewCount.keySet()) {
			redisTemplate.opsForValue().set(COUNT_PREFIX + postId, String.valueOf(updateViewCount.get(postId)));
			redisTemplate.opsForSet().add(DIRTY_SET, postId.toString());
		}

		List<UUID> postIds = new ArrayList<>(posts.stream()
			.map(Post::getId)
			.toList());

		redisTemplate.opsForValue().set(COUNT_PREFIX + postIds.getLast().toString(), "2");

		// when
		postAsyncWorker.syncViewCountAllInBatch(postIds);

		// then
		UUID notUpdatedPostId = postIds.removeLast();

		await().atMost(10, TimeUnit.SECONDS)
			.untilAsserted(() ->
				assertAll(
					() -> {
						List<Post> updatedPosts = postRepository.findAllById(postIds);

						assertThat(updatedPosts)
							.extracting("id", "viewCount")
							.containsExactlyInAnyOrderElementsOf(
								updateViewCount.keySet().stream()
									.filter(id -> id != notUpdatedPostId)
									.map(postId -> tuple(postId, updateViewCount.get(postId)))
									.toList()
							);
					},

					() -> {
						Post post = postRepository.findById(notUpdatedPostId)
							.orElseThrow(() -> new IllegalArgumentException("존재하지 않는 게시글"));
						assertThat(post.getViewCount()).isEqualTo(4);
					},

					() -> assertThat(redisTemplate.opsForSet().members(DIRTY_SET))
						.doesNotContainAnyElementsOf(
							postIds.stream()
								.map(UUID::toString)
								.toList()
						)
						.contains(notUpdatedPostId.toString())
				)
			);
	}
}