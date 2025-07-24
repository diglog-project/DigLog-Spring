package api.store.diglog.model.dto.subscribe;

import java.util.UUID;

import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class SubscriptionCreateRequest {

	@NotNull(message = "구독 대상자를 지정해주세요")
	private UUID authorId;

	@NotNull(message = "알림 여부를 설정해주세요")
	private Boolean notificationEnabled;

}
