package api.store.diglog.common.scheduler;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import api.store.diglog.service.post.PostService;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class SyncScheduler {

	private final PostService postService;

	@Scheduled(fixedDelay = 10_000)
	public void syncPostViewCount() {
		postService.syncPostViewCountToDb();
	}
}
