package api.store.diglog.model.dto.subscribe;

import java.time.LocalDateTime;
import java.util.UUID;

import api.store.diglog.model.entity.Subscription;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder(access = AccessLevel.PRIVATE)
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PROTECTED)
public class SubscriberResponse {

	private UUID subscriptionId;
	private String subscriberUsername;
	private boolean notificationEnabled;
	private LocalDateTime createdAt;

	public static SubscriberResponse from(Subscription subscription) {
		return SubscriberResponse.builder()
			.subscriptionId(subscription.getId())
			.subscriberUsername(subscription.getSubscriber().getUsername())
			.notificationEnabled(subscription.isNotificationEnabled())
			.createdAt(subscription.getCreatedAt())
			.build();
	}
}
