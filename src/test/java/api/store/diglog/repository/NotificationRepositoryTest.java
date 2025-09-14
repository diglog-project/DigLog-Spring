package api.store.diglog.repository;

import static api.store.diglog.model.entity.notification.NotificationType.*;
import static org.assertj.core.api.Assertions.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.transaction.annotation.Transactional;

import api.store.diglog.model.constant.Platform;
import api.store.diglog.model.constant.Role;
import api.store.diglog.model.entity.Member;
import api.store.diglog.model.entity.notification.Notification;
import api.store.diglog.supporter.IntegrationTestSupport;

@Transactional
class NotificationRepositoryTest extends IntegrationTestSupport {

	@DisplayName("사용자의 알림 목록(페이지 단위)을 조회할 수 있다")
	@Test
	void findAllByReceiver() {
		// Given
		Member receiver = createMember("receiver");
		memberRepository.save(receiver);
		int readCount = 3, unreadCount = 7;
		List<Notification> notifications = new ArrayList<>();

		for (int i = 0; i < readCount; i++) {
			Notification notification = notificationRepository.save(createNotification(receiver, true));
			notifications.add(notification);
		}
		for (int i = 0; i < unreadCount; i++) {
			Notification notification = notificationRepository.save(createNotification(receiver, false));
			notifications.add(notification);
		}

		PageRequest pageRequest = PageRequest.of(0, 10,
			Sort.by(Sort.Direction.DESC, "createdAt").and(Sort.by(Sort.Direction.DESC, "id"))
		);

		// When
		Page<Notification> findNotifications = notificationRepository.findAllByReceiver(receiver, pageRequest);

		// Then
		assertThat(findNotifications.getContent()).hasSize(readCount + unreadCount)
			.extracting(Notification::getId)
			.containsExactlyInAnyOrderElementsOf(
				notifications.stream().map(Notification::getId).toList()
			);
	}

	@DisplayName("사용자가 읽지 않은 알림 목록을 조회할 수 있다")
	@Test
	void findAllByReceiverAndIsReadFalse() {
		// Given
		Member receiver = createMember("receiver");
		memberRepository.save(receiver);
		int readCount = 3, unreadCount = 7;
		List<Notification> unreadNotifications = new ArrayList<>();

		for (int i = 0; i < readCount; i++) {
			notificationRepository.save(createNotification(receiver, true));
		}
		for (int i = 0; i < unreadCount; i++) {
			Notification notification = notificationRepository.save(createNotification(receiver, false));
			unreadNotifications.add(notification);
		}

		// When
		List<Notification> notifications = notificationRepository.findAllByReceiverAndIsReadFalse(receiver);

		// Then
		assertThat(notifications).hasSize(unreadCount)
			.extracting(Notification::getId)
			.containsExactlyInAnyOrderElementsOf(
				unreadNotifications.stream().map(Notification::getId).toList()
			);
	}

	@DisplayName("읽지 않은 알람의 개수를 집계할 수 있다")
	@Test
	void countByReceiverAndIsReadFalse() {
		// Given
		Member receiver = createMember("receiver");
		memberRepository.save(receiver);
		int readCount = 3, unreadCount = 7;
		for (int i = 0; i < readCount; i++) {
			notificationRepository.save(createNotification(receiver, true));
		}
		for (int i = 0; i < unreadCount; i++) {
			notificationRepository.save(createNotification(receiver, false));
		}

		// When
		long count = notificationRepository.countByReceiverAndIsReadFalse(receiver);

		// Then
		assertThat(count).isEqualTo(unreadCount);
	}

	private Notification createNotification(Member receiver, boolean isRead) {
		return Notification.builder()
			.id(UUID.randomUUID())
			.receiver(receiver)
			.notificationType(POST_CREATION)
			.message("게시글 생성 알림")
			.isRead(isRead)
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