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
public class SubscriptionResponse {

	private UUID subscriptionId;
	private UUID authorId;
	private String authorUsername;
	private boolean notificationEnabled;
	private LocalDateTime createdAt;

	public static SubscriptionResponse from(Subscription subscription) {
		return SubscriptionResponse.builder()
			.subscriptionId(subscription.getId())
			.authorId(subscription.getAuthor().getId())
			.authorUsername(subscription.getAuthor().getUsername())
			.notificationEnabled(subscription.isNotificationEnabled())
			.createdAt(subscription.getCreatedAt())
			.build();
	}
}
