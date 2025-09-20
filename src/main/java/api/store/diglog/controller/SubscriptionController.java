package api.store.diglog.controller;

import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
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

import api.store.diglog.model.dto.subscribe.SubscriberResponse;
import api.store.diglog.model.dto.subscribe.SubscriptionCreateRequest;
import api.store.diglog.model.dto.subscribe.SubscriptionCreateResponse;
import api.store.diglog.model.dto.subscribe.SubscriptionExistsResponse;
import api.store.diglog.model.dto.subscribe.SubscriptionNotificationActivationRequest;
import api.store.diglog.model.dto.subscribe.SubscriptionResponse;
import api.store.diglog.service.SubscriptionService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/subscriptions")
@RequiredArgsConstructor
@Validated
public class SubscriptionController {

	private final SubscriptionService subscriptionService;

	@GetMapping("/subscribers/{subscriberName}")
	public ResponseEntity<Page<SubscriptionResponse>> getUserSubscriptions(
		@PathVariable("subscriberName") String subscriberName,
		@RequestParam(name = "page", defaultValue = "0") @Min(0) int page,
		@RequestParam(name = "size", defaultValue = "20") @Min(1) int size
	) {
		Page<SubscriptionResponse> response = subscriptionService.getUserSubscriptions(subscriberName, page, size);
		return ResponseEntity.ok().body(response);
	}

	@GetMapping("/authors/{authorName}")
	public ResponseEntity<Page<SubscriberResponse>> getAuthorSubscribers(
		@PathVariable("authorName") String authorName,
		@RequestParam(name = "page", defaultValue = "0") @Min(0) int page,
		@RequestParam(name = "size", defaultValue = "20") @Min(1) int size
	) {
		Page<SubscriberResponse> response = subscriptionService.getAuthorSubscribers(authorName, page, size);
		return ResponseEntity.ok().body(response);
	}

	@GetMapping("/current-member/authors/{authorName}")
	public ResponseEntity<SubscriptionExistsResponse> checkSubscription(
		@PathVariable("authorName") String authorName
	) {
		SubscriptionExistsResponse subscriptionExistsResponse = subscriptionService.checkSubscription(authorName);
		return ResponseEntity.ok().body(subscriptionExistsResponse);
	}

	@PostMapping
	public ResponseEntity<SubscriptionCreateResponse> create(
		@RequestBody @Valid SubscriptionCreateRequest request
	) {
		SubscriptionCreateResponse response = subscriptionService.create(request);
		return ResponseEntity.status(HttpStatus.CREATED).body(response);
	}

	@PatchMapping("/{subscriptionId}/notification-setting")
	public ResponseEntity<Void> updateNotificationSetting(
		@PathVariable("subscriptionId") UUID subscriptionId,
		@RequestBody @Valid SubscriptionNotificationActivationRequest request
	) {
		subscriptionService.updateNotificationSetting(subscriptionId, request);
		return ResponseEntity.noContent().build();
	}

	@DeleteMapping("/{subscriptionId}")
	public ResponseEntity<Void> cancel(@PathVariable("subscriptionId") UUID subscriptionId) {
		subscriptionService.cancel(subscriptionId);
		return ResponseEntity.noContent().build();
	}
}