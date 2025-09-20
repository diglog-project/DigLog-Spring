package api.store.diglog.model.dto.notification;

import java.time.LocalDateTime;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonProperty;

import api.store.diglog.model.entity.notification.Notification;
import api.store.diglog.model.entity.notification.NotificationType;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder(access = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PROTECTED)
public class NotificationResponse {

	private UUID notificationId;

	private NotificationType notificationType;

	private String message;

	@JsonProperty("isRead")
	private boolean isRead;

	private LocalDateTime createdAt;

	public static NotificationResponse from(Notification notification) {
		return NotificationResponse.builder()
			.notificationId(notification.getId())
			.notificationType(notification.getNotificationType())
			.message(notification.getMessage())
			.isRead(notification.isRead())
			.createdAt(notification.getCreatedAt())
			.build();
	}

}
