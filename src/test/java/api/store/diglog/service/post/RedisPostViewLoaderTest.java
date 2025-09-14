package api.store.diglog.service.post;

import static org.assertj.core.api.Assertions.*;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import api.store.diglog.model.constant.Platform;
import api.store.diglog.model.constant.Role;
import api.store.diglog.model.entity.Folder;
import api.store.diglog.model.entity.Member;
import api.store.diglog.model.entity.Post;
import api.store.diglog.supporter.IntegrationTestSupport;

class RedisPostViewLoaderTest extends IntegrationTestSupport {

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

	@DisplayName("레디스에 게시글의 조회수가 없는 경우, DB에서 조회해와서 레디스에 조회수를 적재한다.")
	@Test
	void loadPostViewIntoRedis_shouldStoreViewCount_whenNotExistInRedis() {
		// given
		Post post = postRepository.save(Post.builder()
			.member(member)
			.title("Diglog Redis 적용기")
			.content("Diglog 프로젝트의 Redis 적용과정")
			.viewCount(1000L)
			.folder(folder)
			.build());
		String redisKey = "post:view:count:" + post.getId();

		// when
		redisPostViewLoader.load(redisKey, post.getId());

		// then
		String redisValue = redisTemplate.opsForValue().get(redisKey);
		assertThat(redisValue).isEqualTo("1000");
	}

	@DisplayName("레디스에 게시글의 조회수가 있는 경우, 레디스의 조회수가 덮어씌워지지 않는다.")
	@Test
	void loadPostViewIntoRedis_shouldNotOverwrite_whenExistsInRedis() {
		// given
		Post post = postRepository.save(Post.builder()
			.member(member)
			.title("Diglog Redis 적용기")
			.content("Diglog 프로젝트의 Redis 적용과정")
			.viewCount(1000L)
			.folder(folder)
			.build());

		String redisKey = "post:view:count:" + post.getId();
		redisTemplate.opsForValue().set(redisKey, "9999");

		// when
		redisPostViewLoader.load(redisKey, post.getId());

		// then
		assertThat(redisTemplate.opsForValue().get(redisKey)).isEqualTo("9999");
	}
}