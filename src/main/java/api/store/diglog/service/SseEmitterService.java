package api.store.diglog.service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import api.store.diglog.model.entity.Member;
import api.store.diglog.repository.SseEmitterRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class SseEmitterService {

	private static final Long DEFAULT_TIMEOUT = 60L * 1000 * 60;
	private static final String INITIAL_SEND_NAME = "connect";
	private static final String INITIAL_SEND_MESSAGE = "connected";
	private static final int RECONNECT_TIME_MILLIS = 3000;
	private static final String NOTIFICATION_EVENT_NAME = "notify";

	private final SseEmitterRepository sseEmitterRepository;
	private final MemberService memberService;

	public SseEmitter subscribe() {
		Member currentMember = memberService.getCurrentMember();
		UUID userId = currentMember.getId();
		SseEmitter sseEmitter = new SseEmitter(DEFAULT_TIMEOUT);
		sseEmitterRepository.save(userId, sseEmitter);

		try {
			sseEmitter.send(SseEmitter.event()
				.name(INITIAL_SEND_NAME)
				.data(INITIAL_SEND_MESSAGE)
				.reconnectTime(RECONNECT_TIME_MILLIS)
			);
		} catch (IOException e) {
			sseEmitter.completeWithError(e);
			return sseEmitter;
		}

		sseEmitter.onCompletion(() -> sseEmitterRepository.deleteBy(userId, sseEmitter));
		sseEmitter.onTimeout(() -> {
			sseEmitterRepository.deleteBy(userId, sseEmitter);
			sseEmitter.complete();
		});
		sseEmitter.onError(error -> {
			sseEmitterRepository.deleteBy(userId, sseEmitter);
			sseEmitter.completeWithError(error);
		});

		return sseEmitter;
	}

	public void send(UUID userId, String message) {
		List<SseEmitter> sseEmitters = new ArrayList<>(sseEmitterRepository.findById(userId));
		if (sseEmitters.isEmpty()) {
			return;
		}

		for (SseEmitter sseEmitter : sseEmitters) {
			try {
				sseEmitter.send(SseEmitter.event()
					.name(NOTIFICATION_EVENT_NAME)
					.data(message)
				);
			} catch (IOException | IllegalStateException e) {
				log.warn("SSE 전송 실패: userId={}, emitter={}", userId, sseEmitter, e);
				sseEmitterRepository.deleteBy(userId, sseEmitter);
				sseEmitter.completeWithError(e);
			}
		}
	}

}
