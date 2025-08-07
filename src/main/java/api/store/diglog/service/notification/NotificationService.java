package api.store.diglog.service.notification;

import java.util.List;

import org.springframework.stereotype.Service;

import api.store.diglog.model.dto.notification.NotificationCreateRequest;
import api.store.diglog.model.entity.notification.Notification;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class NotificationService {

	private final NotificationTransactionService notificationTransactionService;
	private final NotificationPublisher notificationPublisher;

	public void createAndPublish(NotificationCreateRequest request) {
		List<Notification> notifications = notificationTransactionService.create(request);
		notificationPublisher.publish(notifications);
	}
}
