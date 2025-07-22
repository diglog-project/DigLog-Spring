package api.store.diglog.repository;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

import org.springframework.stereotype.Repository;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@Repository
public class SseEmitterRepository {

	private final Map<UUID, List<SseEmitter>> emitters = new ConcurrentHashMap<>();

	public List<SseEmitter> findById(UUID userId) {
		return emitters.getOrDefault(userId, new CopyOnWriteArrayList<>());
	}

	public SseEmitter save(UUID userId, SseEmitter sseEmitter) {
		emitters.computeIfAbsent(userId, k -> new CopyOnWriteArrayList<>()).add(sseEmitter);
		return sseEmitter;
	}

	public void deleteBy(UUID userId, SseEmitter sseEmitter) {
		List<SseEmitter> list = emitters.get(userId);
		if (list == null) {
			return;
		}

		list.remove(sseEmitter);

		if (list.isEmpty()) {
			emitters.remove(userId);
		}
	}

}
