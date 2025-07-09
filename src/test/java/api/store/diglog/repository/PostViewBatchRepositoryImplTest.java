package api.store.diglog.repository;

import static org.assertj.core.api.Assertions.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import api.store.diglog.model.constant.Platform;
import api.store.diglog.model.constant.Role;
import api.store.diglog.model.entity.Folder;
import api.store.diglog.model.entity.Member;
import api.store.diglog.model.entity.Post;

@SpringBootTest
@ActiveProfiles("test")
class PostViewBatchRepositoryImplTest {

	@Autowired
	private PostViewBatchRepository postViewBatchRepository;

	@Autowired
	private PostRepository postRepository;

	@Autowired
	private MemberRepository memberRepository;

	@Autowired
	private FolderRepository folderRepository;

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
	}

	@DisplayName("여러 게시글의 조회수를 일괄적으로 업데이트 할 수 있다.")
	@Test
	void bulkUpdateViewCounts() {

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

		Map<UUID, Long> updatePosts = posts.stream()
			.collect(Collectors.toMap(
				Post::getId,
				post -> post.getViewCount() + 10
			));

		// when
		postViewBatchRepository.bulkUpdateViewCounts(updatePosts);

		// then
		List<Post> resultPosts = postRepository.findAllById(
			posts.stream()
				.map(Post::getId)
				.toList()
		);

		assertThat(resultPosts)
			.extracting("id", "viewCount")
			.containsExactlyInAnyOrderElementsOf(
				updatePosts.keySet()
					.stream()
					.map(postId -> tuple(postId, updatePosts.get(postId)))
					.toList()
			);

	}

}