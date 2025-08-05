package api.store.diglog.service.notification;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import api.store.diglog.model.entity.notification.NotificationType;

@Component
public class NotificationStrategyFactory {

	private final Map<NotificationType, NotificationStrategy> notificationStrategies;

	@Autowired
	public NotificationStrategyFactory(List<NotificationStrategy> notificationStrategies) {
		this.notificationStrategies = notificationStrategies.stream()
			.collect(Collectors.toMap(
				NotificationStrategy::getType,
				notificationStrategy -> notificationStrategy
			));
	}

	public NotificationStrategy getStrategy(NotificationType notificationType) {
		return notificationStrategies.get(notificationType);
	}

}
