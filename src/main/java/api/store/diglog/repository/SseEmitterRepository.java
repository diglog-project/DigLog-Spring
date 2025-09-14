package api.store.diglog.repository;

import static java.util.Collections.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

import org.springframework.stereotype.Repository;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@Repository
public class SseEmitterRepository {
	private static final List<SseEmitter> EMPTY_SSE_EMITTER_LIST = emptyList();

	private final Map<UUID, List<SseEmitter>> emitters = new ConcurrentHashMap<>();

	public List<SseEmitter> findById(UUID userId) {
		return unmodifiableList(emitters.getOrDefault(userId, EMPTY_SSE_EMITTER_LIST));
	}

	public SseEmitter save(UUID userId, SseEmitter sseEmitter) {
		emitters.computeIfAbsent(userId, userKey -> new CopyOnWriteArrayList<>()).add(sseEmitter);
		return sseEmitter;
	}

	public void deleteBy(UUID userId, SseEmitter sseEmitter) {
		emitters.computeIfPresent(userId, (userKey, emitterList) -> {
			emitterList.remove(sseEmitter);

			if (emitterList.isEmpty()) {
				return null;
			}
			return emitterList;
		});
	}

}
