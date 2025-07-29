package api.store.diglog.service.notification;

import java.nio.charset.StandardCharsets;
import java.util.List;

import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;

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
			String json = new String(message.getBody(), StandardCharsets.UTF_8);
			List<NotificationPayload> payloads = objectMapper.readValue(
				json, new TypeReference<>() {
				}
			);

			payloads.forEach(payload ->
				sseEmitterService.send(payload.getReceiverId(), payload.getMessage())
			);

		} catch (Exception e) {
			log.error("Failed to process notification payload", e);
		}
	}

}
