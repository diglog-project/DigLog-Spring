package api.store.diglog.service.notification;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;

import api.store.diglog.model.dto.notification.NotificationCreateRequest;
import api.store.diglog.model.entity.Member;
import api.store.diglog.model.entity.notification.Notification;
import api.store.diglog.model.entity.notification.NotificationType;
import api.store.diglog.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class NotificationTransactionService {

	private final NotificationRepository notificationRepository;
	private final NotificationStrategyFactory notificationStrategyFactory;

	public List<Notification> create(NotificationCreateRequest request) {
		NotificationType notificationType = NotificationType.from(request.getNotificationType());
		NotificationStrategy strategy = notificationStrategyFactory.getStrategy(notificationType);

		UUID dataId = request.getDataId();
		List<Member> receivers = strategy.resolveReceivers(dataId);
		String message = strategy.generateMessage(dataId);

		List<Notification> notifications = receivers.stream()
			.map(receiver -> Notification.builder()
				.id(UUID.randomUUID())
				.receiver(receiver)
				.notificationType(notificationType)
				.message(message)
				.isRead(false)
				.build())
			.toList();

		return notificationRepository.saveAll(notifications);
	}
}
