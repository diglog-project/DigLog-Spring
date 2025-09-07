package api.store.diglog.service.notification;

import static org.mockito.Mockito.*;
import static org.testcontainers.shaded.org.awaitility.Awaitility.*;

import java.time.Duration;
import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mock;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import api.store.diglog.model.dto.notification.NotificationPayload;
import api.store.diglog.service.SseEmitterService;
import api.store.diglog.supporter.RedisTestSupporter;
import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.Appender;

@SpringBootTest
@ActiveProfiles("test")
class NotificationSubscriberTest extends RedisTestSupporter {

	@Autowired
	private StringRedisTemplate redisTemplate;

	@MockitoSpyBean
	private SseEmitterService spySseEmitterService;

	@Autowired
	private ChannelTopic notificationChannelTopic;

	@Mock
	private Appender<ILoggingEvent> mockAppender;

	private static final ObjectMapper objectMapper = new ObjectMapper();

	@DisplayName("Redis PUB/SUB을 이용해 서버에서 알림 메세지를 수신할 수 있다.")
	@Test
	void onMessage() throws Exception {
		// given
		UUID receiverId = UUID.randomUUID();
		NotificationPayload payload = NotificationPayload.builder()
			.receiverId(receiverId)
			.message("Hello from Redis!")
			.build();

		String json = objectMapper.writeValueAsString(List.of(payload));

		// when
		redisTemplate.convertAndSend(notificationChannelTopic.getTopic(), json);

		// then
		await().atMost(Duration.ofSeconds(3)).untilAsserted(() -> verify(spySseEmitterService, atLeastOnce())
			.send(eq(payload.getReceiverId()), eq(payload.getMessage()))
		);
	}

	@DisplayName("잘못된 알림 페이로드를 넘길 경우, 에러를 삼키고 로그를 남긴다")
	@ParameterizedTest(name = "Invalid payload case: {1}")
	@MethodSource("provideInvalidNotificationPayload")
	void onMessage_WithInvalidNotificationPayload(String jsonPayload, String expectedLogMessage) {
		// given
		Logger logger = (Logger)LoggerFactory.getLogger(NotificationSubscriber.class);
		logger.addAppender(mockAppender);

		// when
		redisTemplate.convertAndSend(notificationChannelTopic.getTopic(), jsonPayload);

		// then
		await().atMost(Duration.ofSeconds(3)).untilAsserted(() -> {
			verify(spySseEmitterService, times(0)).send(any(), anyString());
			verify(mockAppender, atLeastOnce()).doAppend(argThat(event ->
				event.getLevel() == Level.WARN &&
					event.getMessage().contains(expectedLogMessage)
			));
		});

		logger.detachAppender(mockAppender);
	}

	@DisplayName("여러 개의 알림 페이로드를 한 번에 처리할 수 있다.")
	@Test
	void onMessage_MultiplePayloads_AllProcessed() throws Exception {
		// given
		UUID receiverId1 = UUID.randomUUID();
		UUID receiverId2 = UUID.randomUUID();
		UUID receiverId3 = UUID.randomUUID();

		List<NotificationPayload> payloads = List.of(
			NotificationPayload.builder()
				.receiverId(receiverId1)
				.message("Message 1")
				.build(),
			NotificationPayload.builder()
				.receiverId(receiverId2)
				.message("Message 2")
				.build(),
			NotificationPayload.builder()
				.receiverId(receiverId3)
				.message("Message 3")
				.build()
		);

		String json = objectMapper.writeValueAsString(payloads);

		// when
		redisTemplate.convertAndSend(notificationChannelTopic.getTopic(), json);

		// then
		await().atMost(Duration.ofSeconds(3)).untilAsserted(() -> {
			verify(spySseEmitterService, times(1)).send(eq(receiverId1), eq("Message 1"));
			verify(spySseEmitterService, times(1)).send(eq(receiverId2), eq("Message 2"));
			verify(spySseEmitterService, times(1)).send(eq(receiverId3), eq("Message 3"));
		});
	}

	@DisplayName("한 알림 처리에 예외가 발생해도 다른 알림 처리에 영향을 주지 않는다.")
	@Test
	void onMessage_SseServiceException_ContinuesProcessing() throws Exception {
		// given
		Logger logger = (Logger)LoggerFactory.getLogger(NotificationSubscriber.class);
		logger.addAppender(mockAppender);

		UUID receiverId1 = UUID.randomUUID();
		UUID receiverId2 = UUID.randomUUID();

		List<NotificationPayload> payloads = List.of(
			NotificationPayload.builder()
				.receiverId(receiverId1)
				.message("First message")
				.build(),
			NotificationPayload.builder()
				.receiverId(receiverId2)
				.message("Second message")
				.build()
		);
		String json = objectMapper.writeValueAsString(payloads);

		doThrow(new RuntimeException("SSE connection failed"))
			.when(spySseEmitterService).send(eq(receiverId1), anyString());
		doNothing()
			.when(spySseEmitterService).send(eq(receiverId2), anyString());

		// when
		redisTemplate.convertAndSend(notificationChannelTopic.getTopic(), json);

		// then
		await().atMost(Duration.ofSeconds(3)).untilAsserted(() -> {
			verify(spySseEmitterService, times(1)).send(eq(receiverId1), eq("First message"));
			verify(spySseEmitterService, times(1)).send(eq(receiverId2), eq("Second message"));

			verify(mockAppender, atLeastOnce()).doAppend(argThat(event ->
				event.getLevel() == Level.WARN &&
					event.getMessage().contains("Failed to deliver notification to receiverId=")
			));
		});

		logger.detachAppender(mockAppender);
	}

	static Stream<Arguments> provideInvalidNotificationPayload() throws JsonProcessingException {
		return Stream.of(
			Arguments.of("[null]", "Skip delivering notification: payload is null"),
			Arguments.of(objectMapper.writeValueAsString((List.of(NotificationPayload.builder()
					.receiverId(null)
					.message("valid message")
					.build()))),
				"Skip delivering notification: receiverId is null"),
			Arguments.of(objectMapper.writeValueAsString(List.of(NotificationPayload.builder()
					.receiverId(UUID.randomUUID())
					.message(null)
					.build())),
				"Skip delivering notification: message is blank."),
			Arguments.of(objectMapper.writeValueAsString(List.of(NotificationPayload.builder()
					.receiverId(UUID.randomUUID())
					.message("   ")
					.build())),
				"Skip delivering notification: message is blank.")
		);
	}
}