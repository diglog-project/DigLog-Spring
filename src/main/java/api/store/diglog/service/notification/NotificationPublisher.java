package api.store.diglog.service.notification;

import java.util.List;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.listener.ChannelTopic;
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

	private static final int BATCH_SIZE = 20;

	private final StringRedisTemplate redisTemplate;
	private final ChannelTopic notificationChannel;
	private final ObjectMapper objectMapper;

	public void publish(List<Notification> notifications) {
		BatchPartition<Notification> batches = BatchPartition.of(notifications, BATCH_SIZE);

		batches.stream()
			.forEach(batch -> {
				List<NotificationPayload> payloads = batch.stream()
					.map(notification -> NotificationPayload.builder()
						.receiverId(notification.getReceiver().getId())
						.message(notification.getMessage())
						.build())
					.toList();

				try {
					String jsonPayload = objectMapper.writeValueAsString(payloads);
					redisTemplate.convertAndSend(notificationChannel.getTopic(), jsonPayload);
				} catch (JsonProcessingException e) {
					log.error("Failed to serialize payloads", e);
				}
			});
	}
}
