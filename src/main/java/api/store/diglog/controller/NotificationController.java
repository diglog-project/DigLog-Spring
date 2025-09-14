package api.store.diglog.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import api.store.diglog.model.dto.notification.NotificationCreateRequest;
import api.store.diglog.model.dto.notification.NotificationDeleteRequest;
import api.store.diglog.model.dto.notification.NotificationDeleteResponse;
import api.store.diglog.model.dto.notification.NotificationReadResponse;
import api.store.diglog.model.dto.notification.NotificationResponse;
import api.store.diglog.model.dto.notification.NotificationUnreadCountResponse;
import api.store.diglog.service.SseEmitterService;
import api.store.diglog.service.notification.NotificationService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
@Validated
public class NotificationController {

	private final SseEmitterService sseEmitterService;
	private final NotificationService notificationService;

	@GetMapping(value = "/sse/subscribe", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
	public SseEmitter subscribe() {
		return sseEmitterService.subscribe();
	}

	@GetMapping
	public ResponseEntity<Page<NotificationResponse>> search(
		@RequestParam(name = "page", defaultValue = "0") @Min(0) int page,
		@RequestParam(name = "size", defaultValue = "20") @Min(1) int size
	) {
		Page<NotificationResponse> responses = notificationService.searchBy(page, size);
		return ResponseEntity.ok().body(responses);
	}

	@GetMapping("/unread-count")
	public ResponseEntity<NotificationUnreadCountResponse> countUnreadNotification() {
		NotificationUnreadCountResponse response = notificationService.countUnreadNotification();
		return ResponseEntity.ok().body(response);
	}

	@PostMapping
	public ResponseEntity<Void> create(@RequestBody @Valid NotificationCreateRequest request) {
		notificationService.createAndPublish(request);
		return ResponseEntity.noContent().build();
	}

	@PatchMapping("/{notificationId}/read")
	public ResponseEntity<NotificationReadResponse> markAsRead(@PathVariable("notificationId") UUID notificationId) {
		NotificationReadResponse response = notificationService.markAsRead(notificationId);
		return ResponseEntity.ok().body(response);
	}

	@PatchMapping("/read-all")
	public ResponseEntity<List<NotificationReadResponse>> markAllAsRead() {
		List<NotificationReadResponse> responses = notificationService.markAllAsRead();
		return ResponseEntity.ok().body(responses);
	}

	@DeleteMapping("/{notificationId}")
	public ResponseEntity<Void> delete(@PathVariable("notificationId") UUID notificationId) {
		notificationService.delete(notificationId);
		return ResponseEntity.noContent().build();
	}

	@DeleteMapping
	public ResponseEntity<NotificationDeleteResponse> deleteAll(@RequestBody @Valid NotificationDeleteRequest request) {
		NotificationDeleteResponse response = notificationService.deleteAll(request);
		return ResponseEntity.ok().body(response);
	}
}
