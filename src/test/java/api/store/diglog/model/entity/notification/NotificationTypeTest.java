package api.store.diglog.model.entity.notification;

import static api.store.diglog.model.entity.notification.NotificationType.*;
import static org.assertj.core.api.Assertions.*;

import java.util.Arrays;
import java.util.stream.Stream;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class NotificationTypeTest {

	@DisplayName("알림 타입을 조회할 수 있다")
	@ParameterizedTest(name = "\"{0}\" 입력은 {1} 타입으로 매핑된다")
	@MethodSource("provideNotificationTypes")
	void from(String inputType, NotificationType expectedType) {
		// When
		NotificationType notificationType = NotificationType.from(inputType);

		// Then
		assertThat(notificationType).isSameAs(expectedType);
	}

	@DisplayName("모든 Enum 이름은 from으로 역직렬화된다")
	@ParameterizedTest(name = "{0} -> {1}")
	@MethodSource("provideCanonicalNotificationTypes")
	void from_canonicalNames_roundTrip(String canonical, NotificationType expected) {
		assertThat(NotificationType.from(canonical)).isSameAs(expected);
	}

	private static Stream<Arguments> provideNotificationTypes() {
		return Stream.of(
			Arguments.of("COMMENT_CREATION", COMMENT_CREATION),
			Arguments.of("POST_CREATION", POST_CREATION),
			Arguments.of("post_creation", POST_CREATION),
			Arguments.of("Post_Creation", POST_CREATION),
			Arguments.of("   POST_CREATION     ", POST_CREATION),
			Arguments.of("INVALID", INVALID),
			Arguments.of("존재하지 않는 알림", INVALID),
			Arguments.of("", INVALID),
			Arguments.of("   ", INVALID)
		);
	}

	private static Stream<Arguments> provideCanonicalNotificationTypes() {
		return Arrays.stream(NotificationType.values())
			.filter(t -> t != INVALID)
			.map(t -> Arguments.of(t.name(), t));
	}
}