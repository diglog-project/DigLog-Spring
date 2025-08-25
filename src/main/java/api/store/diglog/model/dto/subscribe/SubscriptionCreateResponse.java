package api.store.diglog.model.dto.subscribe;

import java.time.LocalDateTime;

import api.store.diglog.model.entity.Subscription;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder(access = AccessLevel.PRIVATE)
@Getter
public class SubscriptionCreateResponse {

	private String authorName;
	private String subscriberName;
	private boolean notificationEnabled;
	private LocalDateTime createdAt;

	public static SubscriptionCreateResponse from(Subscription subscription) {
		return SubscriptionCreateResponse.builder()
			.authorName(subscription.getAuthor().getUsername())
			.subscriberName(subscription.getSubscriber().getUsername())
			.notificationEnabled(subscription.isNotificationEnabled())
			.createdAt(subscription.getCreatedAt())
			.build();
	}
}
