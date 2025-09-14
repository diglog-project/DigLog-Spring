package api.store.diglog.service.notification;

import static api.store.diglog.common.exception.ErrorCode.*;
import static api.store.diglog.model.entity.notification.NotificationType.*;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Component;

import api.store.diglog.common.exception.CustomException;
import api.store.diglog.model.entity.Member;
import api.store.diglog.model.entity.notification.NotificationType;

@Component
public class UnsupportedNotificationStrategy implements NotificationStrategy {
	@Override
	public NotificationType getType() {
		return INVALID;
	}

	@Override
	public List<Member> resolveReceivers(UUID dataId) {
		throw new CustomException(NOTIFICATION_INVALID_NOTIFICATION_TYPE);
	}

	@Override
	public String generateMessage(UUID dataId) {
		throw new CustomException(NOTIFICATION_INVALID_NOTIFICATION_TYPE);
	}
}
