package api.store.diglog.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import api.store.diglog.model.dto.subscribe.SubscriberResponse;
import api.store.diglog.model.dto.subscribe.SubscriptionCreateRequest;
import api.store.diglog.model.dto.subscribe.SubscriptionCreateResponse;
import api.store.diglog.model.dto.subscribe.SubscriptionNotificationActivationRequest;
import api.store.diglog.model.dto.subscribe.SubscriptionResponse;
import api.store.diglog.service.SubscriptionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/subscriptions")
@RequiredArgsConstructor
public class SubscriptionController {

	private final SubscriptionService subscriptionService;

	@GetMapping("/users/{userId}")
	public ResponseEntity<List<SubscriptionResponse>> getUserSubscriptions(
		@PathVariable("userId") UUID userId,
		@RequestParam(name = "page", defaultValue = "0") int page,
		@RequestParam(name = "size", defaultValue = "20") int size
	) {
		List<SubscriptionResponse> responses = subscriptionService.getUserSubscriptions(userId, page, size);
		return ResponseEntity.ok().body(responses);
	}

	@GetMapping("/authors/{authorId}")
	public ResponseEntity<List<SubscriberResponse>> getAuthorSubscribers(
		@PathVariable("authorId") UUID authorId,
		@RequestParam(name = "page", defaultValue = "0") int page,
		@RequestParam(name = "size", defaultValue = "20") int size
	) {
		List<SubscriberResponse> responses = subscriptionService.getAuthorSubscribers(authorId, page, size);
		return ResponseEntity.ok().body(responses);
	}

	@PostMapping
	public ResponseEntity<SubscriptionCreateResponse> create(
		@RequestBody @Valid SubscriptionCreateRequest request
	) {
		SubscriptionCreateResponse response = subscriptionService.createSubscription(request);
		return ResponseEntity.ok().body(response);
	}

	@PatchMapping("/{subscriptionId}/notification-setting")
	public ResponseEntity<Void> updateNotificationSetting(
		@PathVariable("subscriptionId") UUID subscriptionId,
		@RequestBody @Valid SubscriptionNotificationActivationRequest request
	) {
		subscriptionService.updateNotificationSetting(subscriptionId, request);
		return ResponseEntity.noContent().build();
	}
}