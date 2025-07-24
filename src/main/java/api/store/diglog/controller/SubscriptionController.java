package api.store.diglog.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import api.store.diglog.model.dto.subscribe.SubscriptionCreateRequest;
import api.store.diglog.model.dto.subscribe.SubscriptionCreateResponse;
import api.store.diglog.service.SubscriptionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/subscribe")
@RequiredArgsConstructor
public class SubscriptionController {

	private final SubscriptionService subscriptionService;

	@PostMapping
	public ResponseEntity<SubscriptionCreateResponse> create(
		@RequestBody @Valid SubscriptionCreateRequest request
	) {
		SubscriptionCreateResponse response = subscriptionService.createSubscription(request);
		return ResponseEntity.ok().body(response);
	}
}