package api.store.diglog.service.notification;

import static org.awaitility.Awaitility.*;
import static org.mockito.Mockito.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.transaction.annotation.Transactional;

import api.store.diglog.model.constant.Platform;
import api.store.diglog.model.constant.Role;
import api.store.diglog.model.entity.Member;
import api.store.diglog.model.entity.notification.Notification;
import api.store.diglog.model.entity.notification.NotificationType;
import api.store.diglog.supporter.IntegrationTestSupport;

@Transactional
class NotificationPublisherTest extends IntegrationTestSupport {

	@Test
	@DisplayName("NotificationPublisher가 Redis Pub/Sub 채널에 메시지를 발행한다")
	void publish() {
		// Given
		Member receiver = createMember("receiver");
		memberRepository.save(receiver);
		Notification notification = createNotification(receiver);

		// When
		notificationPublisher.publish(List.of(notification));

		// Then
		await().atMost(3, TimeUnit.SECONDS).untilAsserted(() ->
			verify(notificationSubscriber, times(1)).onMessage(any(), any())
		);
	}

	@DisplayName("알림은 일정 작업 단위로 나누어서 전송한다")
	@ParameterizedTest(name = "{0}개의 알림은 {1}번 전송된다")
	@CsvSource(value = {
		"1, 1",
		"100, 5",
		"101, 6"}
	)
	void publish_WithBatch(int notificationSize, int expectedCall) {
		// Given
		Member receiver = createMember("receiver");
		memberRepository.save(receiver);
		List<Notification> notifications = new ArrayList<>();
		for (int i = 0; i < notificationSize; i++) {
			notifications.add(createNotification(receiver));
		}

		// When
		notificationPublisher.publish(notifications);

		// Then
		await().atMost(5, TimeUnit.SECONDS).untilAsserted(() ->
			verify(notificationSubscriber, times(expectedCall)).onMessage(any(), any())
		);
	}

	private Notification createNotification(Member receiver) {
		return Notification.builder()
			.id(UUID.randomUUID())
			.receiver(receiver)
			.notificationType(NotificationType.COMMENT_CREATION)
			.message("댓글 생성")
			.isRead(false)
			.build();
	}

	private Member createMember(String userName) {
		return Member.builder()
			.email(userName + "@example.com")
			.username(userName)
			.password(userName + "Password")
			.roles(Set.of(Role.ROLE_USER))
			.platform(Platform.SERVER)
			.createdAt(LocalDateTime.of(2022, 2, 22, 12, 0))
			.updatedAt(LocalDateTime.of(2022, 3, 22, 12, 0))
			.isDeleted(false)
			.build();
	}
}