package api.store.diglog.service.post;

import static api.store.diglog.common.exception.ErrorCode.*;

import java.time.Duration;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import api.store.diglog.common.exception.CustomException;
import api.store.diglog.model.entity.Post;
import api.store.diglog.repository.PostRepository;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class RedisPostViewLoader {

	private static final int LOCK_WAIT_TIME = 3;
	private static final String LOCK_KEY_SUFFIX = ":lock";
	private static final int DAILY_TTL_HOURS = 24;

	private final StringRedisTemplate redisTemplate;
	private final RedissonClient redissonClient;
	private final PostRepository postRepository;

	public void load(String countKey, UUID postId) {
		if (doesNotExistViewCountInRedis(countKey)) {
			RLock viewCountLock = redissonClient.getLock(countKey + LOCK_KEY_SUFFIX);
			boolean isLocked = false;

			try {
				isLocked = viewCountLock.tryLock(LOCK_WAIT_TIME, TimeUnit.SECONDS);

				if (isLocked) {
					loadViewCountFromDBToRedis(countKey, postId);
				}
			} catch (InterruptedException e) {
				throw new CustomException(REDIS_UNAVAILABLE);
			} finally {
				if (isLocked && viewCountLock.isHeldByCurrentThread()) {
					viewCountLock.unlock();
				}
			}
		}

		redisTemplate.expire(countKey, Duration.ofHours(DAILY_TTL_HOURS));
	}

	private void loadViewCountFromDBToRedis(String countKey, UUID postId) {
		if (doesNotExistViewCountInRedis(countKey)) {
			Post post = postRepository.findById(postId)
				.orElseThrow(() -> new CustomException(POST_NOT_FOUND));
			redisTemplate.opsForValue().set(countKey, String.valueOf(post.getViewCount()));
		}
	}

	private boolean doesNotExistViewCountInRedis(String countKey) {
		return !redisTemplate.hasKey(countKey);
	}
}
