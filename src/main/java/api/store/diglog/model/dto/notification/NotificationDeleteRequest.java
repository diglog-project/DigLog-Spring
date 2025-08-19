package api.store.diglog.model.dto.notification;

import java.util.List;
import java.util.UUID;

import jakarta.validation.constraints.NotEmpty;
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
public class NotificationDeleteRequest {

	@NotEmpty(message = "삭제할 알림을 선택해주세요.")
	private List<@NotNull UUID> notificationIds;

}
