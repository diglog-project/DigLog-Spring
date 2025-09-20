package api.store.diglog.service.notification;

import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import api.store.diglog.model.entity.notification.NotificationType;

@Component
public class NotificationStrategyFactory {

	private final Map<NotificationType, NotificationStrategy> notificationStrategies;

	@Autowired
	public NotificationStrategyFactory(List<NotificationStrategy> notificationStrategies) {
		Map<NotificationType, NotificationStrategy> map = new EnumMap<>(NotificationType.class);
		notificationStrategies.forEach(strategy -> map.put(strategy.getType(), strategy));
		this.notificationStrategies = Collections.unmodifiableMap(map);
	}

	public NotificationStrategy getStrategy(NotificationType notificationType) {
		return notificationStrategies.get(notificationType);
	}

}
