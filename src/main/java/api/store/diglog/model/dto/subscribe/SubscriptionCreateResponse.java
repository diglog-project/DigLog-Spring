package api.store.diglog.model.dto.subscribe;

import java.time.LocalDateTime;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
@Getter
public class SubscriptionCreateResponse {

	private String authorNickname;
	private String subscriberNickname;
	private boolean notificationEnabled;
	private LocalDateTime createdAt;

}
