package api.store.diglog.model.dto.subscribe;

import java.util.UUID;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
@Getter
public class SubscriptionExistsResponse {

	private UUID subscriptionId;
	private boolean hasSubscription;

	public static SubscriptionExistsResponse of(UUID subscriptionId, boolean hasSubscription) {
		return SubscriptionExistsResponse.builder()
			.subscriptionId(subscriptionId)
			.hasSubscription(hasSubscription)
			.build();
	}

}
