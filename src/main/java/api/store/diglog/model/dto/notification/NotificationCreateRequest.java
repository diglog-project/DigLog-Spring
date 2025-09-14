package api.store.diglog.model.dto.notification;

import java.util.UUID;

import jakarta.validation.constraints.NotBlank;
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
public class NotificationCreateRequest {

	@NotBlank(message = "알림 타입을 입력해주세요.")
	private String notificationType;

	@NotNull(message = "알림을 보낼 데이터를 입력해주세요.")
	private UUID dataId;

}
