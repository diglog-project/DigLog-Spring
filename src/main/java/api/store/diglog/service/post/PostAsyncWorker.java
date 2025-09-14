package api.store.diglog.service.post;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import api.store.diglog.model.entity.Post;
import api.store.diglog.repository.PostRepository;
import api.store.diglog.repository.PostViewBatchRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class PostAsyncWorker {

	private static final String REDIS_KEY_PREFIX_POST_VIEW = "post:view:";
	private static final String REDIS_KEY_PREFIX_VIEW_COUNT = REDIS_KEY_PREFIX_POST_VIEW + "count:";
	private static final String REDIS_KEY_VIEW_COUNT_DIRTY_SET = REDIS_KEY_PREFIX_POST_VIEW + "dirtySet";

	private static final String LOG_VIEW_COUNT_NULL = "[조회수 부재] postId={}";
	private static final String LOG_VIEW_COUNT_PARSE_FAIL = "[조회수 파싱 실패] postId={}, RedisViewCountValue={}";
	private static final String LOG_VIEW_COUNT_REVERSED = "[조회수 역전 감지] postId={}, Redis={}, DB={}";

	private final StringRedisTemplate redisTemplate;
	private final PostViewBatchRepository postViewBatchRepository;
	private final PostRepository postRepository;

	@Async("syncExecutor")
	public void syncViewCountAllInBatch(List<UUID> postIds) {
		Map<UUID, Long> validViewCounts = extractValidRedisViewCounts(postIds);
		Map<UUID, Long> consistentViewCounts = filterConsistentViewCounts(validViewCounts);
		updateViewCountsAndCleanupDirtySet(consistentViewCounts);
	}

	private Map<UUID, Long> extractValidRedisViewCounts(List<UUID> postIds) {
		return postIds.stream()
			.map(this::createRedisViewCountEntry)
			.filter(Objects::nonNull)
			.collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
	}

	private Map.Entry<UUID, Long> createRedisViewCountEntry(UUID id) {
		String countKey = REDIS_KEY_PREFIX_VIEW_COUNT + id;
		String redisViewCountValue = redisTemplate.opsForValue().get(countKey);

		try {
			if (redisViewCountValue == null) {
				log.error(LOG_VIEW_COUNT_NULL, id);
				return null;
			}

			return Map.entry(id, Long.parseLong(redisViewCountValue));
		} catch (NumberFormatException e) {
			log.error(LOG_VIEW_COUNT_PARSE_FAIL, id, redisViewCountValue);
			return null;
		}
	}

	private Map<UUID, Long> filterConsistentViewCounts(Map<UUID, Long> redisViewCounts) {
		return postRepository.findAllById(redisViewCounts.keySet()).stream()
			.filter(post -> isConsistentViewCount(post, redisViewCounts.get(post.getId())))
			.collect(Collectors.toMap(
				Post::getId,
				post -> redisViewCounts.get(post.getId())
			));
	}

	private boolean isConsistentViewCount(Post post, long redisViewCount) {
		if (post.getViewCount() > redisViewCount) {
			log.error(LOG_VIEW_COUNT_REVERSED, post.getId(), redisViewCount, post.getViewCount());
			return false;
		}
		return true;
	}

	private void updateViewCountsAndCleanupDirtySet(Map<UUID, Long> viewCounts) {
		if (viewCounts.isEmpty()) {
			return;
		}

		postViewBatchRepository.bulkUpdateViewCounts(viewCounts);
		cleanupDirtySet(viewCounts.keySet());
	}

	private void cleanupDirtySet(Set<UUID> processedPostIds) {
		processedPostIds.forEach(postId ->
			redisTemplate.opsForSet().remove(REDIS_KEY_VIEW_COUNT_DIRTY_SET, postId.toString())
		);
	}
}
