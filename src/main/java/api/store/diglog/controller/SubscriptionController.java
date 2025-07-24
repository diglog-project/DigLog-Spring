package api.store.diglog.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import api.store.diglog.model.dto.subscribe.SubscribeCreateRequest;
import api.store.diglog.model.dto.subscribe.SubscribeCreateResponse;
import api.store.diglog.service.SubscriptionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/subscribe")
@RequiredArgsConstructor
public class SubscriptionController {

	private final SubscriptionService subscriptionService;

	@PostMapping
	public ResponseEntity<SubscribeCreateResponse> create(
		@RequestBody @Valid SubscribeCreateRequest request
	) {
		SubscribeCreateResponse subscribeCreateResponse = subscriptionService.createSubscription(request);
		return ResponseEntity.ok().body(subscribeCreateResponse);
	}
}
