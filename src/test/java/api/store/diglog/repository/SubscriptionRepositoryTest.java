package api.store.diglog.repository;

import static java.util.Arrays.*;
import static org.assertj.core.api.Assertions.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import api.store.diglog.model.constant.Platform;
import api.store.diglog.model.constant.Role;
import api.store.diglog.model.entity.Member;
import api.store.diglog.model.entity.Subscription;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class SubscriptionRepositoryTest {

	@Autowired
	MemberRepository memberRepository;

	@Autowired
	SubscriptionRepository subscriptionRepository;

	@DisplayName("구독 id를 이용해 구독 내역을 조회할 수 있다.")
	@Test
	void findByIdFetchSubscriber() {
		// given
		Member frod = Member.builder()
			.email("frod@gmail.com")
			.username("frod")
			.password("frodPassword")
			.roles(Set.of(Role.ROLE_USER, Role.ROLE_ADMIN))
			.platform(Platform.SERVER)
			.createdAt(LocalDateTime.of(2022, 2, 22, 12, 0))
			.updatedAt(LocalDateTime.of(2022, 3, 22, 12, 0))
			.build();
		Member roy = Member.builder()
			.email("roy@gmail.com")
			.username("roy")
			.password("royPassword")
			.roles(Set.of(Role.ROLE_USER, Role.ROLE_ADMIN))
			.platform(Platform.SERVER)
			.createdAt(LocalDateTime.of(2022, 2, 22, 12, 0))
			.updatedAt(LocalDateTime.of(2022, 3, 22, 12, 0))
			.build();
		memberRepository.saveAll(asList(frod, roy));

		Subscription royFrodsubscription = Subscription.builder()
			.id(UUID.randomUUID())
			.author(roy)
			.subscriber(frod)
			.notificationEnabled(true)
			.createdAt(LocalDateTime.of(2022, 2, 22, 12, 0))
			.build();
		subscriptionRepository.save(royFrodsubscription);

		// when
		Subscription subscription = subscriptionRepository.findByIdFetchSubscriber(royFrodsubscription.getId())
			.get();

		// then
		assertThat(subscription).extracting(
			"id", "author", "subscriber", "notificationEnabled"
		).containsExactly(
			royFrodsubscription.getId(),
			royFrodsubscription.getAuthor(),
			royFrodsubscription.getSubscriber(),
			royFrodsubscription.isNotificationEnabled()
		);
	}

	@DisplayName("작성자와 구독자를 이용해 구독 내역을 조회할 수 있다.")
	@Test
	void findByAuthorAndSubscriber() {
		// given
		Member frod = Member.builder()
			.email("frod@gmail.com")
			.username("frod")
			.password("frodPassword")
			.roles(Set.of(Role.ROLE_USER, Role.ROLE_ADMIN))
			.platform(Platform.SERVER)
			.createdAt(LocalDateTime.of(2022, 2, 22, 12, 0))
			.updatedAt(LocalDateTime.of(2022, 3, 22, 12, 0))
			.build();
		Member roy = Member.builder()
			.email("roy@gmail.com")
			.username("roy")
			.password("royPassword")
			.roles(Set.of(Role.ROLE_USER, Role.ROLE_ADMIN))
			.platform(Platform.SERVER)
			.createdAt(LocalDateTime.of(2022, 2, 22, 12, 0))
			.updatedAt(LocalDateTime.of(2022, 3, 22, 12, 0))
			.build();
		memberRepository.saveAll(asList(frod, roy));

		Subscription royFrodsubscription = Subscription.builder()
			.id(UUID.randomUUID())
			.author(roy)
			.subscriber(frod)
			.notificationEnabled(true)
			.createdAt(LocalDateTime.of(2022, 2, 22, 12, 0))
			.build();
		subscriptionRepository.save(royFrodsubscription);

		// when
		Subscription subscription = subscriptionRepository.findByAuthorAndSubscriber(roy, frod)
			.get();

		// then
		assertThat(subscription).extracting(
			"id", "author", "subscriber", "notificationEnabled"
		).containsExactly(
			royFrodsubscription.getId(),
			royFrodsubscription.getAuthor(),
			royFrodsubscription.getSubscriber(),
			royFrodsubscription.isNotificationEnabled()
		);
	}

	@DisplayName("구독자의 구독 수를 조회할 수 있다.")
	@Test
	void countBySubscriber() {
		Member frod = Member.builder()
			.email("frod@gmail.com")
			.username("frod")
			.password("frodPassword")
			.roles(Set.of(Role.ROLE_USER, Role.ROLE_ADMIN))
			.platform(Platform.SERVER)
			.createdAt(LocalDateTime.of(2022, 2, 22, 12, 0))
			.updatedAt(LocalDateTime.of(2022, 3, 22, 12, 0))
			.build();
		Member roy = Member.builder()
			.email("roy@gmail.com")
			.username("roy")
			.password("royPassword")
			.roles(Set.of(Role.ROLE_USER, Role.ROLE_ADMIN))
			.platform(Platform.SERVER)
			.createdAt(LocalDateTime.of(2022, 2, 22, 12, 0))
			.updatedAt(LocalDateTime.of(2022, 3, 22, 12, 0))
			.build();
		Member hana = Member.builder()
			.email("hana@gmail.com")
			.username("hana")
			.password("hanaPassword")
			.roles(Set.of(Role.ROLE_USER, Role.ROLE_ADMIN))
			.platform(Platform.SERVER)
			.createdAt(LocalDateTime.of(2022, 2, 22, 12, 0))
			.updatedAt(LocalDateTime.of(2022, 3, 22, 12, 0))
			.build();
		memberRepository.saveAll(asList(frod, roy, hana));

		Subscription royFrodsubscription = Subscription.builder()
			.id(UUID.randomUUID())
			.author(roy)
			.subscriber(frod)
			.notificationEnabled(true)
			.createdAt(LocalDateTime.of(2022, 2, 22, 12, 0))
			.build();
		Subscription hanaFrodsubscription = Subscription.builder()
			.id(UUID.randomUUID())
			.author(hana)
			.subscriber(frod)
			.notificationEnabled(true)
			.createdAt(LocalDateTime.of(2022, 2, 22, 12, 0))
			.build();
		Subscription royHanasubscription = Subscription.builder()
			.id(UUID.randomUUID())
			.author(roy)
			.subscriber(hana)
			.notificationEnabled(true)
			.createdAt(LocalDateTime.of(2022, 2, 22, 12, 0))
			.build();
		subscriptionRepository.saveAll(asList(royFrodsubscription, hanaFrodsubscription, royHanasubscription));

		// when
		long subscriptionCount = subscriptionRepository.countBySubscriber(frod);

		// then
		assertThat(subscriptionCount).isEqualTo(2);

	}

	@DisplayName("구독자의 구독 목록을 페이지 단위로 조회할 수 있다.")
	@Test
	void findAllBySubscriberAndAuthorIsDeletedFalse() {
		// given
		Member frod = Member.builder()
			.email("frod@gmail.com")
			.username("frod")
			.password("frodPassword")
			.roles(Set.of(Role.ROLE_USER, Role.ROLE_ADMIN))
			.platform(Platform.SERVER)
			.createdAt(LocalDateTime.of(2022, 2, 22, 12, 0))
			.updatedAt(LocalDateTime.of(2022, 3, 22, 12, 0))
			.build();
		Member roy = Member.builder()
			.email("roy@gmail.com")
			.username("roy")
			.password("royPassword")
			.roles(Set.of(Role.ROLE_USER, Role.ROLE_ADMIN))
			.platform(Platform.SERVER)
			.createdAt(LocalDateTime.of(2022, 2, 22, 12, 0))
			.updatedAt(LocalDateTime.of(2022, 3, 22, 12, 0))
			.build();
		Member hana = Member.builder()
			.email("hana@gmail.com")
			.username("hana")
			.password("hanaPassword")
			.roles(Set.of(Role.ROLE_USER, Role.ROLE_ADMIN))
			.platform(Platform.SERVER)
			.createdAt(LocalDateTime.of(2022, 2, 22, 12, 0))
			.updatedAt(LocalDateTime.of(2022, 3, 22, 12, 0))
			.build();
		memberRepository.saveAll(asList(frod, roy, hana));

		Subscription royFrodsubscription = Subscription.builder()
			.id(UUID.randomUUID())
			.author(roy)
			.subscriber(frod)
			.notificationEnabled(true)
			.createdAt(LocalDateTime.of(2022, 2, 22, 12, 0))
			.build();
		Subscription hanaFrodsubscription = Subscription.builder()
			.id(UUID.randomUUID())
			.author(hana)
			.subscriber(frod)
			.notificationEnabled(true)
			.createdAt(LocalDateTime.of(2022, 2, 22, 12, 0))
			.build();
		Subscription royHanasubscription = Subscription.builder()
			.id(UUID.randomUUID())
			.author(roy)
			.subscriber(hana)
			.notificationEnabled(true)
			.createdAt(LocalDateTime.of(2022, 2, 22, 12, 0))
			.build();
		subscriptionRepository.saveAll(asList(royFrodsubscription, hanaFrodsubscription, royHanasubscription));

		PageRequest pageRequest = PageRequest.of(0, 10, Sort.by("createdAt").descending());

		// when
		Page<Subscription> subscriptionPage = subscriptionRepository
			.findAllBySubscriberAndAuthorIsDeletedFalse(frod, pageRequest);

		// then
		assertThat(subscriptionPage.getContent())
			.hasSize(2)
			.extracting("id", "author", "subscriber", "notificationEnabled")
			.contains(
				tuple(
					royFrodsubscription.getId(),
					royFrodsubscription.getAuthor(),
					royFrodsubscription.getSubscriber(),
					royFrodsubscription.isNotificationEnabled()
				),
				tuple(
					hanaFrodsubscription.getId(),
					hanaFrodsubscription.getAuthor(),
					hanaFrodsubscription.getSubscriber(),
					hanaFrodsubscription.isNotificationEnabled()
				)
			);
	}

	@DisplayName("구독자의 구독 목록(페이지 단위) 조회시 탈퇴한 작성자에 대한 구독은 포함되지 않는다.")
	@Test
	void findAllBySubscriberAndAuthorIsDeletedFalse_WithDeletedMember() {
		// given
		Member frod = Member.builder()
			.email("frod@gmail.com")
			.username("frod")
			.password("frodPassword")
			.roles(Set.of(Role.ROLE_USER, Role.ROLE_ADMIN))
			.platform(Platform.SERVER)
			.createdAt(LocalDateTime.of(2022, 2, 22, 12, 0))
			.updatedAt(LocalDateTime.of(2022, 3, 22, 12, 0))
			.build();
		Member roy = Member.builder()
			.email("roy@gmail.com")
			.username("roy")
			.password("royPassword")
			.roles(Set.of(Role.ROLE_USER, Role.ROLE_ADMIN))
			.platform(Platform.SERVER)
			.createdAt(LocalDateTime.of(2022, 2, 22, 12, 0))
			.updatedAt(LocalDateTime.of(2022, 3, 22, 12, 0))
			.build();
		Member hana = Member.builder()
			.email("hana@gmail.com")
			.username("hana")
			.password("hanaPassword")
			.roles(Set.of(Role.ROLE_USER, Role.ROLE_ADMIN))
			.platform(Platform.SERVER)
			.createdAt(LocalDateTime.of(2022, 2, 22, 12, 0))
			.updatedAt(LocalDateTime.of(2022, 3, 22, 12, 0))
			.isDeleted(true)
			.build();
		memberRepository.saveAll(asList(frod, roy, hana));

		Subscription royFrodsubscription = Subscription.builder()
			.id(UUID.randomUUID())
			.author(roy)
			.subscriber(frod)
			.notificationEnabled(true)
			.createdAt(LocalDateTime.of(2022, 2, 22, 12, 0))
			.build();
		Subscription hanaFrodsubscription = Subscription.builder()
			.id(UUID.randomUUID())
			.author(hana)
			.subscriber(frod)
			.notificationEnabled(true)
			.createdAt(LocalDateTime.of(2022, 2, 22, 12, 0))
			.build();
		Subscription royHanasubscription = Subscription.builder()
			.id(UUID.randomUUID())
			.author(roy)
			.subscriber(hana)
			.notificationEnabled(true)
			.createdAt(LocalDateTime.of(2022, 2, 22, 12, 0))
			.build();
		subscriptionRepository.saveAll(asList(royFrodsubscription, hanaFrodsubscription, royHanasubscription));

		PageRequest pageRequest = PageRequest.of(0, 10, Sort.by("createdAt").descending());

		// when
		Page<Subscription> subscriptionPage = subscriptionRepository
			.findAllBySubscriberAndAuthorIsDeletedFalse(frod, pageRequest);

		// then
		assertThat(subscriptionPage.getContent())
			.hasSize(1)
			.extracting("id", "author", "subscriber", "notificationEnabled")
			.contains(
				tuple(
					royFrodsubscription.getId(),
					royFrodsubscription.getAuthor(),
					royFrodsubscription.getSubscriber(),
					royFrodsubscription.isNotificationEnabled()
				)
			);
	}

	@DisplayName("작성자의 구독 목록을 페이지 단위로 조회할 수 있다.")
	@Test
	void findAllByAuthorAndSubscriberIsDeletedFalse() {
		// given
		Member frod = Member.builder()
			.email("frod@gmail.com")
			.username("frod")
			.password("frodPassword")
			.roles(Set.of(Role.ROLE_USER, Role.ROLE_ADMIN))
			.platform(Platform.SERVER)
			.createdAt(LocalDateTime.of(2022, 2, 22, 12, 0))
			.updatedAt(LocalDateTime.of(2022, 3, 22, 12, 0))
			.build();
		Member roy = Member.builder()
			.email("roy@gmail.com")
			.username("roy")
			.password("royPassword")
			.roles(Set.of(Role.ROLE_USER, Role.ROLE_ADMIN))
			.platform(Platform.SERVER)
			.createdAt(LocalDateTime.of(2022, 2, 22, 12, 0))
			.updatedAt(LocalDateTime.of(2022, 3, 22, 12, 0))
			.build();
		Member hana = Member.builder()
			.email("hana@gmail.com")
			.username("hana")
			.password("hanaPassword")
			.roles(Set.of(Role.ROLE_USER, Role.ROLE_ADMIN))
			.platform(Platform.SERVER)
			.createdAt(LocalDateTime.of(2022, 2, 22, 12, 0))
			.updatedAt(LocalDateTime.of(2022, 3, 22, 12, 0))
			.build();
		memberRepository.saveAll(asList(frod, roy, hana));

		Subscription royFrodsubscription = Subscription.builder()
			.id(UUID.randomUUID())
			.author(roy)
			.subscriber(frod)
			.notificationEnabled(true)
			.createdAt(LocalDateTime.of(2022, 2, 22, 12, 0))
			.build();
		Subscription hanaFrodsubscription = Subscription.builder()
			.id(UUID.randomUUID())
			.author(hana)
			.subscriber(frod)
			.notificationEnabled(true)
			.createdAt(LocalDateTime.of(2022, 2, 22, 12, 0))
			.build();
		Subscription royHanasubscription = Subscription.builder()
			.id(UUID.randomUUID())
			.author(roy)
			.subscriber(hana)
			.notificationEnabled(true)
			.createdAt(LocalDateTime.of(2022, 2, 22, 12, 0))
			.build();
		subscriptionRepository.saveAll(asList(royFrodsubscription, hanaFrodsubscription, royHanasubscription));

		PageRequest pageRequest = PageRequest.of(0, 10, Sort.by("createdAt").descending());

		// when
		Page<Subscription> subscriptionPage = subscriptionRepository
			.findAllByAuthorAndSubscriberIsDeletedFalse(roy, pageRequest);

		// then
		assertThat(subscriptionPage.getContent())
			.hasSize(2)
			.extracting("id", "author", "subscriber", "notificationEnabled")
			.contains(
				tuple(
					royFrodsubscription.getId(),
					royFrodsubscription.getAuthor(),
					royFrodsubscription.getSubscriber(),
					royFrodsubscription.isNotificationEnabled()
				),
				tuple(
					royHanasubscription.getId(),
					royHanasubscription.getAuthor(),
					royHanasubscription.getSubscriber(),
					royHanasubscription.isNotificationEnabled()
				)
			);
	}

	@DisplayName("작성자의 구독 목록(페이지 단위) 조회시 탈퇴한 구독자에 대한 구독은 포함되지 않는다.")
	@Test
	void findAllByAuthorAndSubscriberIsDeletedFalse_WithDeletedMember() {
		// given
		Member frod = Member.builder()
			.email("frod@gmail.com")
			.username("frod")
			.password("frodPassword")
			.roles(Set.of(Role.ROLE_USER, Role.ROLE_ADMIN))
			.platform(Platform.SERVER)
			.createdAt(LocalDateTime.of(2022, 2, 22, 12, 0))
			.updatedAt(LocalDateTime.of(2022, 3, 22, 12, 0))
			.build();
		Member roy = Member.builder()
			.email("roy@gmail.com")
			.username("roy")
			.password("royPassword")
			.roles(Set.of(Role.ROLE_USER, Role.ROLE_ADMIN))
			.platform(Platform.SERVER)
			.createdAt(LocalDateTime.of(2022, 2, 22, 12, 0))
			.updatedAt(LocalDateTime.of(2022, 3, 22, 12, 0))
			.build();
		Member hana = Member.builder()
			.email("hana@gmail.com")
			.username("hana")
			.password("hanaPassword")
			.roles(Set.of(Role.ROLE_USER, Role.ROLE_ADMIN))
			.platform(Platform.SERVER)
			.createdAt(LocalDateTime.of(2022, 2, 22, 12, 0))
			.updatedAt(LocalDateTime.of(2022, 3, 22, 12, 0))
			.isDeleted(true)
			.build();
		memberRepository.saveAll(asList(frod, roy, hana));

		Subscription royFrodsubscription = Subscription.builder()
			.id(UUID.randomUUID())
			.author(roy)
			.subscriber(frod)
			.notificationEnabled(true)
			.createdAt(LocalDateTime.of(2022, 2, 22, 12, 0))
			.build();
		Subscription hanaFrodsubscription = Subscription.builder()
			.id(UUID.randomUUID())
			.author(hana)
			.subscriber(frod)
			.notificationEnabled(true)
			.createdAt(LocalDateTime.of(2022, 2, 22, 12, 0))
			.build();
		Subscription royHanasubscription = Subscription.builder()
			.id(UUID.randomUUID())
			.author(roy)
			.subscriber(hana)
			.notificationEnabled(true)
			.createdAt(LocalDateTime.of(2022, 2, 22, 12, 0))
			.build();
		subscriptionRepository.saveAll(asList(royFrodsubscription, hanaFrodsubscription, royHanasubscription));

		PageRequest pageRequest = PageRequest.of(0, 10, Sort.by("createdAt").descending());

		// when
		Page<Subscription> subscriptionPage = subscriptionRepository
			.findAllByAuthorAndSubscriberIsDeletedFalse(roy, pageRequest);

		// then
		assertThat(subscriptionPage.getContent())
			.hasSize(1)
			.extracting("id", "author", "subscriber", "notificationEnabled")
			.contains(
				tuple(
					royFrodsubscription.getId(),
					royFrodsubscription.getAuthor(),
					royFrodsubscription.getSubscriber(),
					royFrodsubscription.isNotificationEnabled()
				)
			);
	}

	@DisplayName("작성자의 구독 목록을 조회할 수 있다.")
	@Test
	void testFindAllByAuthorAndSubscriberIsDeletedFalse() {
		// given
		Member frod = Member.builder()
			.email("frod@gmail.com")
			.username("frod")
			.password("frodPassword")
			.roles(Set.of(Role.ROLE_USER, Role.ROLE_ADMIN))
			.platform(Platform.SERVER)
			.createdAt(LocalDateTime.of(2022, 2, 22, 12, 0))
			.updatedAt(LocalDateTime.of(2022, 3, 22, 12, 0))
			.build();
		Member roy = Member.builder()
			.email("roy@gmail.com")
			.username("roy")
			.password("royPassword")
			.roles(Set.of(Role.ROLE_USER, Role.ROLE_ADMIN))
			.platform(Platform.SERVER)
			.createdAt(LocalDateTime.of(2022, 2, 22, 12, 0))
			.updatedAt(LocalDateTime.of(2022, 3, 22, 12, 0))
			.build();
		Member hana = Member.builder()
			.email("hana@gmail.com")
			.username("hana")
			.password("hanaPassword")
			.roles(Set.of(Role.ROLE_USER, Role.ROLE_ADMIN))
			.platform(Platform.SERVER)
			.createdAt(LocalDateTime.of(2022, 2, 22, 12, 0))
			.updatedAt(LocalDateTime.of(2022, 3, 22, 12, 0))
			.build();
		memberRepository.saveAll(asList(frod, roy, hana));

		Subscription royFrodsubscription = Subscription.builder()
			.id(UUID.randomUUID())
			.author(roy)
			.subscriber(frod)
			.notificationEnabled(true)
			.createdAt(LocalDateTime.of(2022, 2, 22, 12, 0))
			.build();
		Subscription hanaFrodsubscription = Subscription.builder()
			.id(UUID.randomUUID())
			.author(hana)
			.subscriber(frod)
			.notificationEnabled(true)
			.createdAt(LocalDateTime.of(2022, 2, 22, 12, 0))
			.build();
		Subscription royHanasubscription = Subscription.builder()
			.id(UUID.randomUUID())
			.author(roy)
			.subscriber(hana)
			.notificationEnabled(true)
			.createdAt(LocalDateTime.of(2022, 2, 22, 12, 0))
			.build();
		subscriptionRepository.saveAll(asList(royFrodsubscription, hanaFrodsubscription, royHanasubscription));

		// when
		List<Subscription> subscriptions = subscriptionRepository
			.findAllByAuthorAndSubscriberIsDeletedFalse(roy);

		// then
		assertThat(subscriptions)
			.hasSize(2)
			.extracting("id", "author", "subscriber", "notificationEnabled")
			.contains(
				tuple(
					royFrodsubscription.getId(),
					royFrodsubscription.getAuthor(),
					royFrodsubscription.getSubscriber(),
					royFrodsubscription.isNotificationEnabled()
				),
				tuple(
					royHanasubscription.getId(),
					royHanasubscription.getAuthor(),
					royHanasubscription.getSubscriber(),
					royHanasubscription.isNotificationEnabled()
				)
			);
	}

	@DisplayName("작성자의 구독 목록 조회시 탈퇴한 구독자에 대한 구독은 포함되지 않는다.")
	@Test
	void testFindAllByAuthorAndSubscriberIsDeletedFalse_WithDeletedMember() {
		// given
		Member frod = Member.builder()
			.email("frod@gmail.com")
			.username("frod")
			.password("frodPassword")
			.roles(Set.of(Role.ROLE_USER, Role.ROLE_ADMIN))
			.platform(Platform.SERVER)
			.createdAt(LocalDateTime.of(2022, 2, 22, 12, 0))
			.updatedAt(LocalDateTime.of(2022, 3, 22, 12, 0))
			.build();
		Member roy = Member.builder()
			.email("roy@gmail.com")
			.username("roy")
			.password("royPassword")
			.roles(Set.of(Role.ROLE_USER, Role.ROLE_ADMIN))
			.platform(Platform.SERVER)
			.createdAt(LocalDateTime.of(2022, 2, 22, 12, 0))
			.updatedAt(LocalDateTime.of(2022, 3, 22, 12, 0))
			.build();
		Member hana = Member.builder()
			.email("hana@gmail.com")
			.username("hana")
			.password("hanaPassword")
			.roles(Set.of(Role.ROLE_USER, Role.ROLE_ADMIN))
			.platform(Platform.SERVER)
			.createdAt(LocalDateTime.of(2022, 2, 22, 12, 0))
			.updatedAt(LocalDateTime.of(2022, 3, 22, 12, 0))
			.isDeleted(true)
			.build();
		memberRepository.saveAll(asList(frod, roy, hana));

		Subscription royFrodsubscription = Subscription.builder()
			.id(UUID.randomUUID())
			.author(roy)
			.subscriber(frod)
			.notificationEnabled(true)
			.createdAt(LocalDateTime.of(2022, 2, 22, 12, 0))
			.build();
		Subscription hanaFrodsubscription = Subscription.builder()
			.id(UUID.randomUUID())
			.author(hana)
			.subscriber(frod)
			.notificationEnabled(true)
			.createdAt(LocalDateTime.of(2022, 2, 22, 12, 0))
			.build();
		Subscription royHanasubscription = Subscription.builder()
			.id(UUID.randomUUID())
			.author(roy)
			.subscriber(hana)
			.notificationEnabled(true)
			.createdAt(LocalDateTime.of(2022, 2, 22, 12, 0))
			.build();
		subscriptionRepository.saveAll(asList(royFrodsubscription, hanaFrodsubscription, royHanasubscription));

		// when
		List<Subscription> subscriptions = subscriptionRepository
			.findAllByAuthorAndSubscriberIsDeletedFalse(roy);

		// then
		assertThat(subscriptions)
			.hasSize(1)
			.extracting("id", "author", "subscriber", "notificationEnabled")
			.contains(
				tuple(
					royFrodsubscription.getId(),
					royFrodsubscription.getAuthor(),
					royFrodsubscription.getSubscriber(),
					royFrodsubscription.isNotificationEnabled()
				)
			);
	}
}