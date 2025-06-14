package api.store.diglog.service.post;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import api.store.diglog.repository.PostViewBatchRepository;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class PostAsyncWorker {

	private static final String REDIS_KEY_PREFIX_POST_VIEW = "post:view:";
	private static final String REDIS_KEY_PREFIX_VIEW_COUNT = REDIS_KEY_PREFIX_POST_VIEW + "count:";
	private static final String REDIS_KEY_VIEW_COUNT_DIRTY_SET = REDIS_KEY_PREFIX_POST_VIEW + "dirtySet";

	private final RedisTemplate<String, String> redisTemplate;
	private final PostViewBatchRepository postViewBatchRepository;

	@Async("syncExecutor")
	public void syncViewCountAllInBatch(List<UUID> postIds) {

		Map<UUID, Long> viewCounts = postIds.stream()
			.map(id -> {
				String countKey = REDIS_KEY_PREFIX_VIEW_COUNT + id;
				String value = redisTemplate.opsForValue().get(countKey);
				try {
					return Map.entry(id, Long.parseLong(value));
				} catch (NumberFormatException e) {
					return null;
				}
			})
			.filter(Objects::nonNull)
			.collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

		if (!viewCounts.isEmpty()) {
			postViewBatchRepository.bulkUpdateViewCounts(viewCounts);
			viewCounts.keySet().forEach(postId ->
				redisTemplate.opsForSet().remove(REDIS_KEY_VIEW_COUNT_DIRTY_SET, postId.toString())
			);
		}
	}

}
