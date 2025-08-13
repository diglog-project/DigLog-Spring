package api.store.diglog.model.dto.subscribe;

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

	private boolean hasSubscription;

	public static SubscriptionExistsResponse of(boolean hasSubscription) {
		return SubscriptionExistsResponse.builder()
			.hasSubscription(hasSubscription)
			.build();
	}

}
