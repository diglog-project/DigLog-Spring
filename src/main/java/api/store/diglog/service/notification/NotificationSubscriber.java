package api.store.diglog.service.notification;

import java.util.List;

import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import api.store.diglog.model.dto.notification.NotificationPayload;
import api.store.diglog.service.SseEmitterService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class NotificationSubscriber implements MessageListener {

	private final SseEmitterService sseEmitterService;
	private final ObjectMapper objectMapper;

	@Override
	public void onMessage(Message message, @Nullable byte[] pattern) {
		try {
			List<NotificationPayload> payloads = objectMapper.readValue(
				message.getBody(), new TypeReference<>() {
				}
			);

			payloads.forEach(this::sendMessage);

		} catch (JsonProcessingException e) {
			log.error("Failed to deserialize notification payload", e);
		} catch (Exception e) {
			log.error("Failed to process notification payload", e);
		}
	}

	private void sendMessage(NotificationPayload payload) {
		if (isInvalidPayload(payload)) {
			return;
		}

		try {
			sseEmitterService.send(payload.getReceiverId(), payload.getMessage());
		} catch (Exception ex) {
			log.warn("Failed to deliver notification to receiverId={}", payload.getReceiverId(), ex);
		}
	}

	private boolean isInvalidPayload(NotificationPayload payload) {
		if (payload == null) {
			log.warn("Skip delivering notification: payload is null");
			return true;
		}
		if (payload.getReceiverId() == null) {
			log.warn("Skip delivering notification: receiverId is null. payload={}", payload);
			return true;
		}
		if (payload.getMessage() == null || payload.getMessage().isBlank()) {
			log.warn("Skip delivering notification: message is blank. receiverId={}", payload.getReceiverId());
			return true;
		}
		return false;
	}

}
