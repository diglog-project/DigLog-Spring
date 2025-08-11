package api.store.diglog.model.dto.notification;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder(access = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PROTECTED)
public class NotificationDeleteResponse {

	private int requestedCount;
	private int deletedCount;

	public static NotificationDeleteResponse of(int requestedCount, int deletedCount) {
		return NotificationDeleteResponse.builder()
			.requestedCount(requestedCount)
			.deletedCount(deletedCount)
			.build();
	}
}
