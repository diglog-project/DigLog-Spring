package api.store.diglog.model.dto.notification;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder(access = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PROTECTED)
public class NotificationUnreadCountResponse {

	private long unreadCount;

	public static NotificationUnreadCountResponse of(long unreadCount) {
		return NotificationUnreadCountResponse.builder()
			.unreadCount(unreadCount)
			.build();
	}
}
