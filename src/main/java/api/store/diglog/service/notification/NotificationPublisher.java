package api.store.diglog.service.notification;

import java.util.List;
import java.util.UUID;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import api.store.diglog.common.util.BatchPartition;
import api.store.diglog.model.dto.notification.NotificationPayload;
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

	public void publish(List<UUID> subscriberIds, String message) {
		BatchPartition<UUID> batches = BatchPartition.of(subscriberIds, BATCH_SIZE);

		batches.stream()
			.forEach(batch -> {
				List<NotificationPayload> payloads = batch.stream()
					.map(id -> NotificationPayload.builder()
						.receiverId(id)
						.message(message)
						.build())
					.toList();

				// JSON으로 변환해서 전송 (String 전송 권장)
				String jsonPayload;
				try {
					jsonPayload = objectMapper.writeValueAsString(payloads);
					redisTemplate.convertAndSend("notification-channel", jsonPayload);
				} catch (JsonProcessingException e) {
					log.error("Failed to serialize payloads", e);
				}
			});
	}
}
