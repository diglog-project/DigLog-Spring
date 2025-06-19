package api.store.diglog.service;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.testcontainers.shaded.org.awaitility.Awaitility.*;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import api.store.diglog.common.exception.CustomException;
import api.store.diglog.model.constant.Platform;
import api.store.diglog.model.constant.Role;
import api.store.diglog.model.dto.post.PostViewIncrementRequest;
import api.store.diglog.model.dto.post.PostViewResponse;
import api.store.diglog.model.entity.Folder;
import api.store.diglog.model.entity.Member;
import api.store.diglog.model.entity.Post;
import api.store.diglog.repository.FolderRepository;
import api.store.diglog.repository.MemberRepository;
import api.store.diglog.repository.PostRepository;
import api.store.diglog.repository.PostViewBatchRepository;
import api.store.diglog.service.post.PostService;

@Testcontainers
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class PostServiceTest {
	@Container
	static GenericContainer<?> redisContainer = new GenericContainer<>("redis:7.2")
		.withExposedPorts(6379)
		.waitingFor(Wait.forListeningPort());

	@DynamicPropertySource
	static void overrideRedisProperties(DynamicPropertyRegistry registry) {
		registry.add("spring.redis.host", redisContainer::getHost);
		registry.add("spring.redis.port", () -> redisContainer.getMappedPort(6379));
	}

	@Autowired
	private MemberRepository memberRepository;

	@Autowired
	private FolderRepository folderRepository;

	@MockitoSpyBean
	private PostRepository postRepository;

	@Autowired
	private PostService postService;

	@MockitoSpyBean
	private PostViewBatchRepository postViewBatchRepository;

	@Autowired
	private RedisTemplate<String, String> redisTemplate;

	@Autowired
	private RedissonClient redissonClient;

	private Member member;

	private Folder folder;

	@BeforeEach
	void setUp() {

		member = memberRepository.save(Member.builder()
			.email("Frod@gmail.com")
			.username("Frod")
			.password("FrodPassword")
			.roles(Set.of(Role.ROLE_USER))
			.platform(Platform.SERVER)
			.createdAt(LocalDateTime.of(2022, 2, 22, 12, 0))
			.updatedAt(LocalDateTime.of(2022, 3, 22, 12, 0))
			.build());

		folder = folderRepository.save(Folder.builder()
			.id(UUID.randomUUID())
			.member(member)
			.title("diglog")
			.depth(0)
			.orderIndex(0)
			.parentFolder(null)
			.build());

	}

	@AfterEach
	void tearDown() {
		postRepository.deleteAllInBatch();
		folderRepository.deleteAllInBatch();
		memberRepository.deleteAllInBatch();
		redisTemplate.getConnectionFactory().getConnection().serverCommands().flushAll();
	}

	@DisplayName("게시글 조회수를 조회할 수 있다.")
	@Test
	void getViewCount() {

		// given
		Post post = postRepository.save(Post.builder()
			.member(member)
			.title("Diglog Redis 적용기")
			.content("Diglog 프로젝트의 Redis 적용과정")
			.viewCount(1000L)
			.folder(folder)
			.build());

		// when
		PostViewResponse postViewResponse = postService.getViewCount(post.getId());

		// then
		assertThat(postViewResponse)
			.extracting("postId", "viewCount")
			.containsExactly(post.getId(), post.getViewCount());

		Long ttlSeconds = redisTemplate.getExpire("post:view:count:" + post.getId(), TimeUnit.SECONDS);
		assertThat(ttlSeconds).isGreaterThan(0L);
		assertThat(ttlSeconds).isLessThanOrEqualTo(Duration.ofHours(24).getSeconds());
	}

	@DisplayName("존재하지 않는 게시글의 조회수는 조회할 수 없다.")
	@Test
	void getViewCount_OfNotExistPost() {

		// given
		UUID notExistPostId = UUID.fromString("aaaaaaaa-1111-2222-3333-123456789012");

		// when, then
		assertThatThrownBy(() -> postService.getViewCount(notExistPostId))
			.isInstanceOf(CustomException.class)
			.hasMessage("해당 게시글이 없습니다.");
	}

	@DisplayName("레디스의 조회수를 증가시킬 수 있다.")
	@Test
	void increaseView() {

		// given
		Post post = postRepository.save(Post.builder()
			.member(member)
			.title("Diglog Redis 적용기")
			.content("Diglog 프로젝트의 Redis 적용과정")
			.viewCount(100L)
			.folder(folder)
			.build());

		PostViewIncrementRequest postViewIncrementRequest = PostViewIncrementRequest.builder()
			.postId(post.getId())
			.build();

		// when
		postService.increaseView(postViewIncrementRequest, "10.0.0.1");

		// then
		long viewCount = Long.parseLong(redisTemplate.opsForValue().get("post:view:count:" + post.getId()));
		assertThat(viewCount).isEqualTo(post.getViewCount() + 1);

		Long ttlSeconds = redisTemplate.getExpire("post:view:count:" + post.getId(), TimeUnit.SECONDS);
		assertThat(ttlSeconds).isGreaterThan(0L);
		assertThat(ttlSeconds).isLessThanOrEqualTo(Duration.ofHours(24).getSeconds());
	}

	@DisplayName("조회수 증가 시 dirtySet에 게시글 ID가 추가된다.")
	@Test
	void increaseView_shouldAddToDirtySet() {
		// given
		Post post = postRepository.save(Post.builder()
			.member(member)
			.title("Diglog Redis 적용기")
			.content("Diglog 프로젝트의 Redis 적용과정")
			.viewCount(1000L)
			.folder(folder)
			.build());
		UUID postId = post.getId();
		String ip = "192.168.0.1";
		PostViewIncrementRequest request = PostViewIncrementRequest.builder().postId(postId).build();

		// when
		postService.increaseView(request, ip);

		// then
		Set<String> dirtySet = redisTemplate.opsForSet().members("post:view:dirtySet");
		assertThat(dirtySet).contains(postId.toString());
	}

	@DisplayName("일정 시간 내에 같은 ip에서 조회수 증가를 여러번 요청한 경우, 첫 조회수 요청에서만 조회수가 증가된다.")
	@Test
	void increaseView_WithSameIpAddressRequests() {

		// given
		Post post = postRepository.save(Post.builder()
			.member(member)
			.title("Diglog Redis 적용기")
			.content("Diglog 프로젝트의 Redis 적용과정")
			.viewCount(100L)
			.folder(folder)
			.build());
		UUID postId = post.getId();

		PostViewIncrementRequest postViewIncrementRequest = PostViewIncrementRequest.builder()
			.postId(postId)
			.build();

		String ipAddress = "10.0.0.1";
		String ipKey = "post:view:" + postId + ":" + ipAddress;
		String countKey = "post:view:count:" + postId;

		redisTemplate.opsForValue().set(ipKey, "true", Duration.ofHours(24));
		redisTemplate.opsForValue().set(countKey, String.valueOf(post.getViewCount()), Duration.ofHours(24));

		// when
		postService.increaseView(postViewIncrementRequest, ipAddress);

		// then
		long viewCount = Long.parseLong(redisTemplate.opsForValue().get("post:view:count:" + post.getId()));
		assertThat(viewCount).isEqualTo(post.getViewCount());
	}

	@DisplayName("TTL이 만료된 후에는 같은 ip의 조회수 증가 요청에도 조회수가 증가한다.")
	@Test
	void increaseView_afterTtlExpired() throws InterruptedException {

		// given
		Post post = postRepository.save(Post.builder()
			.member(member)
			.title("Diglog Redis 적용기")
			.content("Diglog 프로젝트의 Redis 적용과정")
			.viewCount(100L)
			.folder(folder)
			.build());

		UUID postId = post.getId();

		PostViewIncrementRequest postViewIncrementRequest = PostViewIncrementRequest.builder()
			.postId(postId)
			.build();

		String ipAddress = "10.0.0.1";
		String ipKey = "post:view:" + postId + ":" + ipAddress;
		String countKey = "post:view:count:" + postId;

		redisTemplate.opsForValue().set(ipKey, "true", Duration.ofMillis(10));
		redisTemplate.opsForValue().set(countKey, String.valueOf(post.getViewCount()), Duration.ofHours(24));

		Thread.sleep(100);

		// when
		postService.increaseView(postViewIncrementRequest, ipAddress);

		// then
		long viewCount = Long.parseLong(redisTemplate.opsForValue().get(countKey));
		assertThat(viewCount).isEqualTo(post.getViewCount() + 1);
	}

	@DisplayName("레디스에 조회수가 이미 존재하면 DB를 조회하지 않는다.")
	@Test
	void loadPostViewIntoRedis_shouldNotQueryDb_whenCacheExists() {
		// given
		Post post = postRepository.save(Post.builder()
			.member(member)
			.title("Diglog Redis 적용기")
			.content("Diglog 프로젝트의 Redis 적용과정")
			.viewCount(100L)
			.folder(folder)
			.build());
		UUID postId = post.getId();
		String countKey = "post:view:count:" + postId;
		redisTemplate.opsForValue().set(countKey, "999");

		// when
		postService.getViewCount(postId);

		// then
		verify(postRepository, never()).findById(postId);
	}

	@DisplayName("레디스에 적재되어 있지 않은 게시글의 조회수 증가 동시 요청에도 조회수가 누락되지 않는다.")
	@Test
	void increaseView_withConcurrentRequests_shouldNotMiss() throws Exception {
		// given
		Post post = postRepository.save(Post.builder()
			.title("락 테스트")
			.content("동시성")
			.viewCount(100L)
			.member(member)
			.folder(folder)
			.build());

		UUID postId = post.getId();
		String countKey = "post:view:count:" + postId;
		String redisLockKey = countKey + ":lock";

		int threadCount = 30;
		try (ExecutorService executor = Executors.newFixedThreadPool(threadCount)) {

			CountDownLatch readyLatch = new CountDownLatch(threadCount);
			CountDownLatch startLatch = new CountDownLatch(1);
			CountDownLatch doneLatch = new CountDownLatch(threadCount);

			RLock lock = redissonClient.getLock(redisLockKey);
			lock.lock();

			for (int i = 0; i < threadCount; i++) {
				int userIndex = i;
				executor.submit(() -> {
					try {
						PostViewIncrementRequest request = PostViewIncrementRequest.builder()
							.postId(postId)
							.build();
						String ip = "10.0.0." + userIndex;

						readyLatch.countDown();
						startLatch.await();

						postService.increaseView(request, ip);
						System.out.println(ip + " : " + redisTemplate.opsForValue().get(countKey));

					} catch (InterruptedException e) {
						throw new RuntimeException(e);

					} finally {
						doneLatch.countDown();
					}
				});
			}

			readyLatch.await();

			lock.unlock();
			startLatch.countDown();

			doneLatch.await();
			executor.shutdown();
		}

		// then
		String value = redisTemplate.opsForValue().get(countKey);
		assertThat(value).isNotNull();
		assertThat(Long.parseLong(value)).isEqualTo(post.getViewCount() + threadCount);
	}

	@DisplayName("존재하지 않는 게시글의 조회수는 증가시킬 수 없다.")
	@Test
	void increaseView_OfNotExistPost() {

		// given
		UUID notExistPostId = UUID.fromString("12345678-aaaa-bbbb-cccc-123456789012");
		PostViewIncrementRequest postViewIncrementRequest = PostViewIncrementRequest.builder()
			.postId(notExistPostId)
			.build();

		// when, then
		assertThatThrownBy(() -> postService.increaseView(postViewIncrementRequest, "10.0.0.1"))
			.isInstanceOf(CustomException.class)
			.hasMessage("해당 게시글이 없습니다.");
	}

	@DisplayName("조회수 증가 후 조회 시 증가된 값이 반환된다.")
	@Test
	void getViewCount_afterIncreaseView() {
		// given
		Post post = postRepository.save(Post.builder()
			.member(member)
			.title("Diglog Redis 적용기")
			.content("Diglog 프로젝트의 Redis 적용과정")
			.viewCount(100L)
			.folder(folder)
			.build());
		UUID postId = post.getId();
		PostViewIncrementRequest request = PostViewIncrementRequest.builder()
			.postId(postId)
			.build();

		postService.increaseView(request, "10.0.0.1");

		// when
		PostViewResponse response = postService.getViewCount(postId);

		// then
		assertThat(response.getViewCount()).isEqualTo(post.getViewCount() + 1);
	}

	@DisplayName("조회수 동기화 시 Redis에서 데이터를 읽어와 DB에 반영하고, dirtySet을 초기화한다.")
	@Test
	void syncPostViewCountToDb() throws InterruptedException {
		// given
		Post post = postRepository.save(Post.builder()
			.member(member)
			.title("Diglog Redis 적용기")
			.content("Diglog 프로젝트의 Redis 적용과정")
			.viewCount(100L)
			.folder(folder)
			.build());
		UUID postId = post.getId();

		redisTemplate.opsForValue().set("post:view:count:" + postId, "1234");
		redisTemplate.opsForSet().add("post:view:dirtySet", postId.toString());

		// when
		postService.syncPostViewCountToDb();
		Thread.sleep(1000);

		// then
		Post updated = postRepository.findById(postId).orElseThrow();
		Set<String> dirtySet = redisTemplate.opsForSet().members("post:view:dirtySet");

		assertAll(
			() -> assertThat(updated.getViewCount()).isEqualTo(1234),
			() -> assertThat(dirtySet).doesNotContain(postId.toString())
		);

	}

	@DisplayName("조회수 업데이트는 배치 단위로 분할되어 비동기로 수행한다.")
	@CsvSource({
		"1, 1",
		"100, 1",
		"150, 2",
		"250, 3",
		"1000, 10"
	})
	@ParameterizedTest(name = "{0}개의 게시글은 조회수 업데이트 기능을 {1}번 호출한다")
	void syncPostViewCountToDb_shouldDelegateToWorkerWithCorrectBatches(int totalCounts, int expectedMethodCallCount) {
		// given
		List<Member> members = memberRepository.saveAll(
			IntStream.range(0, totalCounts)
				.mapToObj(i -> Member.builder()
					.email("Frod" + i + "@gmail.com")
					.username("Frod" + i)
					.password("FrodPassword" + i)
					.roles(Set.of(Role.ROLE_USER))
					.platform(Platform.SERVER)
					.createdAt(LocalDateTime.of(2022, 2, 22, 12, 0))
					.updatedAt(LocalDateTime.of(2022, 3, 22, 12, 0))
					.build())
				.toList()
		);
		List<Folder> folders = folderRepository.saveAll(
			IntStream.range(0, totalCounts)
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

		List<Post> posts = IntStream.range(0, totalCounts)
			.mapToObj(i -> postRepository.save(Post.builder()
				.title("title " + i)
				.content("content")
				.viewCount(i)
				.member(members.get(i))
				.folder(folders.get(i))
				.build()))
			.toList();

		posts.forEach(post -> {
			redisTemplate.opsForValue().set("post:view:count:" + post.getId(), String.valueOf(post.getViewCount() * 2));
			redisTemplate.opsForSet().add("post:view:dirtySet", post.getId().toString());
		});

		// when
		postService.syncPostViewCountToDb();

		// then
		await().atMost(5, TimeUnit.SECONDS).untilAsserted(() -> {
			assertAll(
				() -> verify(postViewBatchRepository, times(expectedMethodCallCount)).bulkUpdateViewCounts(any()),
				() -> {
					List<Post> updatedPosts = postRepository.findAllById(
						posts.stream()
							.map(Post::getId)
							.toList()
					);
					assertThat(updatedPosts)
						.extracting("id", "viewCount")
						.containsExactlyInAnyOrderElementsOf(
							updatedPosts.stream()
								.map(Post::getId)
								.map(postId -> tuple(postId,
									Long.parseLong(redisTemplate.opsForValue().get("post:view:count:" + postId))))
								.toList()
						);
				}
			);
		});
	}

}