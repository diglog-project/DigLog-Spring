package api.store.diglog.service.notification;

import java.util.List;
import java.util.UUID;

import api.store.diglog.model.entity.Member;
import api.store.diglog.model.entity.notification.NotificationType;

public interface NotificationStrategy {

	NotificationType getType();

	List<Member> resolveReceivers(UUID dataId);

	String generateMessage(UUID dataId);

}
