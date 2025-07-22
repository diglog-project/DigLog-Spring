package api.store.diglog.controller;

import java.util.UUID;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import api.store.diglog.service.SseEmitterService;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class NotificationController {

	private final SseEmitterService sseEmitterService;

	@GetMapping(value = "/sse/subscribe/{userId}", produces = "text/event-stream")
	public SseEmitter subscribe(@PathVariable("userId") UUID userId) {
		return sseEmitterService.subscribe(userId);
	}
}
