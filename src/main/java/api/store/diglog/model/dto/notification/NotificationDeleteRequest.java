package api.store.diglog.model.dto.notification;

import java.util.List;
import java.util.UUID;

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

	List<UUID> notificationIds;

}
