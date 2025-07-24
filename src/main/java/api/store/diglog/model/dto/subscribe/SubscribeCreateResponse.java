package api.store.diglog.model.dto.subscribe;

import java.time.LocalDateTime;
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
public class SubscribeCreateResponse {

	private UUID authorId;
	private String authorNickname;
	private UUID subscriberId;
	private String subscriberNickname;
	private boolean notificationEnabled;
	private LocalDateTime createdAt;

}
