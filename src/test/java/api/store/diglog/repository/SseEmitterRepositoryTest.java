package api.store.diglog.repository;

import static org.assertj.core.api.Assertions.*;

import java.util.List;
import java.util.Queue;
import java.util.UUID;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

class SseEmitterRepositoryTest {

	@DisplayName("SseEmitter를 조회할 수 있다.")
	@Test
	void findById() {
		// given
		SseEmitterRepository repository = new SseEmitterRepository();

		UUID userId = UUID.randomUUID();
		SseEmitter emitter01 = new SseEmitter();
		SseEmitter emitter02 = new SseEmitter();
		repository.save(userId, emitter01);
		repository.save(userId, emitter02);

		// when
		List<SseEmitter> sseEmitters = repository.findById(userId);

		// then
		assertThat(sseEmitters)
			.hasSize(2)
			.containsExactlyInAnyOrder(emitter01, emitter02);
	}

	@DisplayName("저장된 SseEmitter가 없을 경우 빈 리스트를 반환한다.")
	@Test
	void findById_WithEmpty() {
		// given
		SseEmitterRepository repository = new SseEmitterRepository();

		UUID userId = UUID.randomUUID();

		// when
		List<SseEmitter> sseEmitters = repository.findById(userId);

		// then
		assertThat(sseEmitters).isEmpty();
	}

	@DisplayName("SseEmitter를 저장할 수 있다.")
	@Test
	void save() {
		// given
		SseEmitterRepository repository = new SseEmitterRepository();
		UUID userId = UUID.randomUUID();
		SseEmitter emitter01 = new SseEmitter();

		// when
		repository.save(userId, emitter01);

		// then
		List<SseEmitter> sseEmitters = repository.findById(userId);
		assertThat(sseEmitters)
			.hasSize(1)
			.containsExactly(emitter01);
	}

	@DisplayName("SseEmitter를 삭제할 수 있다.")
	@Test
	void deleteBy() {
		// given
		SseEmitterRepository repository = new SseEmitterRepository();
		UUID userId = UUID.randomUUID();
		SseEmitter emitter01 = new SseEmitter();
		SseEmitter emitter02 = new SseEmitter();
		SseEmitter emitter03 = new SseEmitter();

		repository.save(userId, emitter01);
		repository.save(userId, emitter02);
		repository.save(userId, emitter03);

		// when
		repository.deleteBy(userId, emitter01);

		// then
		List<SseEmitter> sseEmitters = repository.findById(userId);
		assertThat(sseEmitters)
			.hasSize(2)
			.doesNotContain(emitter01)
			.containsExactlyInAnyOrder(emitter02, emitter03);
	}

	@DisplayName("마지막 Emitter 삭제 시 key도 제거된다.")
	@Test
	void deleteBy_removeKeyWhenEmpty() {
		// given
		SseEmitterRepository repository = new SseEmitterRepository();
		UUID userId = UUID.randomUUID();
		SseEmitter emitter = new SseEmitter();
		repository.save(userId, emitter);

		// when
		repository.deleteBy(userId, emitter);

		// then
		assertThat(repository.findById(userId)).isEmpty();
	}

	@DisplayName("멀티스레드 환경에서도 ConcurrentModificationException이 발생하지 않는다.")
	@Test
	void concurrentAccess() throws InterruptedException {
		// given
		SseEmitterRepository repository = new SseEmitterRepository();
		UUID userId = UUID.randomUUID();
		Queue<SseEmitter> emitters = new ConcurrentLinkedQueue<>();

		int threadCount = 30;
		ExecutorService executor = Executors.newFixedThreadPool(threadCount);

		try {
			CountDownLatch readyLatch = new CountDownLatch(threadCount);
			CountDownLatch startLatch = new CountDownLatch(1);
			CountDownLatch doneLatch = new CountDownLatch(threadCount);

			// when
			for (int i = 0; i < threadCount; i++) {
				executor.submit(() -> {
					try {
						SseEmitter sseEmitter = new SseEmitter();
						repository.save(userId, sseEmitter);
						emitters.add(sseEmitter);

						readyLatch.countDown();
						startLatch.await();
					} catch (InterruptedException e) {
						Thread.currentThread().interrupt();
						throw new RuntimeException(e);
					} finally {
						doneLatch.countDown();
					}
				});
			}

			readyLatch.await();
			startLatch.countDown();
			doneLatch.await();
		} finally {
			executor.shutdown();
			if (!executor.awaitTermination(5, TimeUnit.SECONDS)) {
				executor.shutdownNow();
			}
		}

		// then
		List<SseEmitter> findSseEmitters = repository.findById(userId);
		assertThat(findSseEmitters)
			.hasSize(emitters.size())
			.containsExactlyInAnyOrderElementsOf(emitters);
	}

}