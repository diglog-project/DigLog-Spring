package api.store.diglog.service.notification;

import static api.store.diglog.model.entity.notification.NotificationType.*;
import static org.assertj.core.api.Assertions.*;

import java.util.UUID;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import api.store.diglog.common.exception.CustomException;
import api.store.diglog.model.entity.notification.NotificationType;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class UnsupportedNotificationStrategyTest {

	@Autowired
	private UnsupportedNotificationStrategy unsupportedNotificationStrategy;

	@DisplayName("지원하지 않는 알림 전략 객체의 알림 타입을 조회할 수 있다")
	@Test
	void getType() {
		// When
		NotificationType notificationType = unsupportedNotificationStrategy.getType();

		// Then
		assertThat(notificationType).isEqualTo(INVALID);
	}

	@DisplayName("지원하지 않는 알림 전략 객체는 알림 수신자를 조회할 수 없다")
	@Test
	void resolveReceivers() {
		// When, Then
		assertThatThrownBy(() -> unsupportedNotificationStrategy.resolveReceivers(UUID.randomUUID()))
			.isInstanceOf(CustomException.class)
			.hasMessage("지원하지 않는 알림 타입입니다.");
	}

	@DisplayName("지원하지 않는 알림 전략 객체는 알림 메세지를 생성할 수 없다")
	@Test
	void generateMessage() {
		// When, Then
		assertThatThrownBy(() -> unsupportedNotificationStrategy.generateMessage(UUID.randomUUID()))
			.isInstanceOf(CustomException.class)
			.hasMessage("지원하지 않는 알림 타입입니다.");
	}
}