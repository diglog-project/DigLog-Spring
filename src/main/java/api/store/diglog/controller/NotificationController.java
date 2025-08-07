package api.store.diglog.controller;

import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import api.store.diglog.model.dto.notification.NotificationCreateRequest;
import api.store.diglog.service.SseEmitterService;
import api.store.diglog.service.notification.NotificationService;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {

	private final SseEmitterService sseEmitterService;
	private final NotificationService notificationService;

	@GetMapping(value = "/sse/subscribe/{userId}", produces = "text/event-stream")
	public SseEmitter subscribe(@PathVariable("userId") UUID userId) {
		return sseEmitterService.subscribe(userId);
	}

	@PostMapping
	public ResponseEntity<Void> create(@RequestBody NotificationCreateRequest request) {
		notificationService.createAndPublish(request);
		return ResponseEntity.noContent().build();
	}
}
