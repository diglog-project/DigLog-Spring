package api.store.diglog.model.entity.notification;

import static api.store.diglog.model.entity.notification.NotificationType.*;
import static org.assertj.core.api.Assertions.*;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import api.store.diglog.common.exception.CustomException;
import api.store.diglog.model.constant.Platform;
import api.store.diglog.model.constant.Role;
import api.store.diglog.model.entity.Member;

class NotificationTest {

	@DisplayName("알림을 생성할 수 있다")
	@Test
	void create() {
		// Given
		Member receiver = createMember("receiver");
		UUID notificationId = UUID.randomUUID();

		// When
		Notification notification = Notification.builder()
			.id(notificationId)
			.receiver(receiver)
			.notificationType(POST_CREATION)
			.message("게시글 생성 알림")
			.isRead(false)
			.build();

		// Then
		assertThat(notification)
			.extracting("id", "receiver", "notificationType", "message", "isRead")
			.containsExactly(notificationId, receiver, POST_CREATION, "게시글 생성 알림", false);
	}

	@DisplayName("지원하지 않는 알림 타입으로는 알림을 생성할 수 없다")
	@Test
	void create_InvalidNotificationType() {
		// Given
		Member receiver = createMember("receiver");
		UUID notificationId = UUID.randomUUID();

		// When, Then
		assertThatThrownBy(() -> Notification.builder()
			.id(notificationId)
			.receiver(receiver)
			.notificationType(INVALID)
			.message("게시글 생성 알림")
			.isRead(false)
			.build())
			.isInstanceOf(CustomException.class)
			.hasMessage("지원하지 않는 알림 타입입니다.");
	}

	@DisplayName("알림을 읽음 처리할 수 있다")
	@Test
	void markAsRead() {
		// Given
		Member receiver = createMember("receiver");
		UUID notificationId = UUID.randomUUID();
		Notification notification = Notification.builder()
			.id(notificationId)
			.receiver(receiver)
			.notificationType(POST_CREATION)
			.message("게시글 생성 알림")
			.isRead(false)
			.build();

		// When
		notification.markAsRead();

		// Then
		assertThat(notification)
			.extracting("id", "receiver", "notificationType", "message", "isRead")
			.containsExactly(notificationId, receiver, POST_CREATION, "게시글 생성 알림", true);
	}

	@DisplayName("읽음 처리는 멱등성 있게 처리된다")
	@ParameterizedTest(name = "{0}번의 읽음 처리를 후 isRead는 {1} -> true로 변한다")
	@CsvSource(value = {
		"1, true",
		"3, false",
		"10, false"
	})
	void markAsRead_Idempotency(int methodCallCount, boolean givenIsRead) {
		// Given
		Member receiver = createMember("receiver");
		UUID notificationId = UUID.randomUUID();
		Notification notification = Notification.builder()
			.id(notificationId)
			.receiver(receiver)
			.notificationType(POST_CREATION)
			.message("게시글 생성 알림")
			.isRead(givenIsRead)
			.build();

		// When
		for (int i = 0; i < methodCallCount; i++) {
			notification.markAsRead();
		}

		// Then
		assertThat(notification)
			.extracting("id", "receiver", "notificationType", "message", "isRead")
			.containsExactly(notificationId, receiver, POST_CREATION, "게시글 생성 알림", true);
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