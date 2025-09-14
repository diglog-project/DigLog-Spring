package api.store.diglog.service.notification;

import static api.store.diglog.model.entity.notification.NotificationType.*;
import static org.assertj.core.api.Assertions.*;

import java.util.stream.Stream;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import api.store.diglog.model.entity.notification.NotificationType;
import api.store.diglog.supporter.IntegrationTestSupport;

class NotificationStrategyFactoryTest extends IntegrationTestSupport {

	@DisplayName("알림 타입에 맞는 전략 객체를 조회할 수 있다.")
	@ParameterizedTest(name = "{0} 타입은 {1} 전략으로 매핑된다")
	@MethodSource("provideNotificationTypesAndStrategies")
	void getStrategy(NotificationType type, Class<? extends NotificationStrategy> expectedClass) {
		// When
		NotificationStrategy strategy = notificationStrategyFactory.getStrategy(type);

		// Then
		assertThat(strategy).isInstanceOf(expectedClass);
	}

	private static Stream<Arguments> provideNotificationTypesAndStrategies() {
		return Stream.of(
			Arguments.of(COMMENT_CREATION, CommentCreationNotificationStrategy.class),
			Arguments.of(POST_CREATION, PostCreationNotificationStrategy.class),
			Arguments.of(INVALID, UnsupportedNotificationStrategy.class)
		);
	}
}