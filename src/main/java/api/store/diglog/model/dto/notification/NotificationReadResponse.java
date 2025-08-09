package api.store.diglog.model.dto.notification;

import java.util.UUID;

import api.store.diglog.model.entity.notification.Notification;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder(access = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PROTECTED)
public class NotificationReadResponse {

	private UUID notificationId;
	private boolean isRead;

	public static NotificationReadResponse from(Notification notification) {
		return NotificationReadResponse.builder()
			.notificationId(notification.getId())
			.isRead(notification.isRead())
			.build();
	}

}
