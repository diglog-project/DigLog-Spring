package api.store.diglog.model.entity;

import static org.assertj.core.api.Assertions.*;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import api.store.diglog.model.constant.Platform;
import api.store.diglog.model.constant.Role;

class SubscriptionTest {
	@DisplayName("구독 알림을 활성화 할 수 있다.")
	@Test
	void enableNotification() {
		// given
		Member author = Member.builder()
			.email("author@gmail.com")
			.username("author")
			.password("authorPassword")
			.roles(Set.of(Role.ROLE_USER, Role.ROLE_ADMIN))
			.platform(Platform.SERVER)
			.createdAt(LocalDateTime.of(2022, 2, 22, 12, 0))
			.updatedAt(LocalDateTime.of(2022, 3, 22, 12, 0))
			.build();
		Member subscriber = Member.builder()
			.email("subscriber@gmail.com")
			.username("subscriber")
			.password("subscriberPassword")
			.roles(Set.of(Role.ROLE_USER))
			.platform(Platform.SERVER)
			.createdAt(LocalDateTime.of(2022, 2, 22, 12, 0))
			.updatedAt(LocalDateTime.of(2022, 3, 22, 12, 0))
			.build();
		Subscription subscription = Subscription.builder()
			.id(UUID.randomUUID())
			.author(author)
			.subscriber(subscriber)
			.notificationEnabled(false)
			.createdAt(LocalDateTime.of(2022, 2, 22, 12, 0))
			.build();

		// when
		subscription.enableNotification();

		// then
		assertThat(subscription.isNotificationEnabled()).isEqualTo(true);
	}

	@DisplayName("구독 알림을 비활성화 할 수 있다.")
	@Test
	void disableNotification() {
		// given
		Member author = Member.builder()
			.email("author@gmail.com")
			.username("author")
			.password("authorPassword")
			.roles(Set.of(Role.ROLE_USER, Role.ROLE_ADMIN))
			.platform(Platform.SERVER)
			.createdAt(LocalDateTime.of(2022, 2, 22, 12, 0))
			.updatedAt(LocalDateTime.of(2022, 3, 22, 12, 0))
			.build();
		Member subscriber = Member.builder()
			.email("subscriber@gmail.com")
			.username("subscriber")
			.password("subscriberPassword")
			.roles(Set.of(Role.ROLE_USER))
			.platform(Platform.SERVER)
			.createdAt(LocalDateTime.of(2022, 2, 22, 12, 0))
			.updatedAt(LocalDateTime.of(2022, 3, 22, 12, 0))
			.build();
		Subscription subscription = Subscription.builder()
			.id(UUID.randomUUID())
			.author(author)
			.subscriber(subscriber)
			.notificationEnabled(true)
			.createdAt(LocalDateTime.of(2022, 2, 22, 12, 0))
			.build();

		// when
		subscription.disableNotification();

		// then
		assertThat(subscription.isNotificationEnabled()).isEqualTo(false);
	}
}