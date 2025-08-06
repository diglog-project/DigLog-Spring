package api.store.diglog.service.notification;

import java.util.List;
import java.util.UUID;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import api.store.diglog.common.util.BatchPartition;
import api.store.diglog.model.dto.notification.NotificationPayload;
import api.store.diglog.model.entity.notification.Notification;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationPublisher {

	private static final String REDIS_NOTIFICATION_CHANNEL = "notification-channel";
	private static final int BATCH_SIZE = 20;

	private final RedisTemplate<String, String> redisTemplate;
	private final ObjectMapper objectMapper;

	public void publish(UUID subscriberId, String message) {
		redisTemplate.convertAndSend(
			REDIS_NOTIFICATION_CHANNEL,
			NotificationPayload.builder()
				.receiverId(subscriberId)
				.message(message)
				.build()
		);
	}

	public void publish(List<Notification> notifications) {
		BatchPartition<Notification> batches = BatchPartition.of(notifications, BATCH_SIZE);

		batches.stream()
			.forEach(batch -> {
				List<NotificationPayload> payloads = batch.stream()
					.map(notification -> NotificationPayload.builder()
						.receiverId(notification.getId())
						.message(notification.getMessage())
						.build())
					.toList();

				try {
					String jsonPayload = objectMapper.writeValueAsString(payloads);
					redisTemplate.convertAndSend("notification-channel", jsonPayload);
				} catch (JsonProcessingException e) {
					log.error("Failed to serialize payloads", e);
				}
			});
	}
}
