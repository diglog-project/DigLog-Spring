package api.store.diglog.service;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import api.store.diglog.model.entity.Member;
import api.store.diglog.repository.SseEmitterRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class SseEmitterService {

	private static final Long DEFAULT_TIMEOUT = 60L * 1000 * 60;
	private static final String NOTIFICATION_EVENT_NAME = "notify";

	private final SseEmitterRepository sseEmitterRepository;
	private final MemberService memberService;

	public SseEmitter subscribe() {
		Member currentMember = memberService.getCurrentMember();
		UUID userId = currentMember.getId();
		SseEmitter sseEmitter = new SseEmitter(DEFAULT_TIMEOUT);
		sseEmitterRepository.save(userId, sseEmitter);

		sseEmitter.onCompletion(() -> sseEmitterRepository.deleteBy(userId, sseEmitter));
		sseEmitter.onTimeout(() -> sseEmitterRepository.deleteBy(userId, sseEmitter));
		sseEmitter.onError(error -> sseEmitterRepository.deleteBy(userId, sseEmitter));

		return sseEmitter;
	}

	public void send(UUID userId, String message) {
		List<SseEmitter> sseEmitters = sseEmitterRepository.findById(userId);
		if (sseEmitters.isEmpty()) {
			return;
		}

		for (SseEmitter sseEmitter : sseEmitters) {
			try {
				sseEmitter.send(SseEmitter.event()
					.name(NOTIFICATION_EVENT_NAME)
					.data(message)
				);
			} catch (IOException e) {
				sseEmitterRepository.deleteBy(userId, sseEmitter);
				sseEmitter.completeWithError(e);
			}
		}
	}

}
