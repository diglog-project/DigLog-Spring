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
import api.store.diglog.supporter.RedisTestSupporter;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class SubscriptionRepositoryTest extends RedisTestSupporter {

	@Autowired
	MemberRepository memberRepository;

	@Autowired
	SubscriptionRepository subscriptionRepository;

	@DisplayName("구독 id를 이용해 구독 내역을 조회할 수 있다.")
	@Test
	void findByIdFetchSubscriber() {
		// given
		Member frod = createMember("frod", false);
		Member roy = createMember("roy", false);
		memberRepository.saveAll(asList(frod, roy));

		Subscription royFrodSubscription = createSubscription(roy, frod);
		subscriptionRepository.save(royFrodSubscription);

		// when
		Subscription subscription = subscriptionRepository.findByIdFetchSubscriber(royFrodSubscription.getId())
			.get();

		// then
		assertThat(subscription).extracting(
			"id", "author", "subscriber", "notificationEnabled"
		).containsExactly(
			royFrodSubscription.getId(),
			royFrodSubscription.getAuthor(),
			royFrodSubscription.getSubscriber(),
			royFrodSubscription.isNotificationEnabled()
		);
	}

	@DisplayName("작성자와 구독자를 이용해 구독 내역을 조회할 수 있다.")
	@Test
	void findByAuthorAndSubscriber() {
		// given
		Member frod = createMember("frod", false);
		Member roy = createMember("roy", false);
		memberRepository.saveAll(asList(frod, roy));

		Subscription royFrodSubscription = createSubscription(roy, frod);
		subscriptionRepository.save(royFrodSubscription);

		// when
		Subscription subscription = subscriptionRepository.findByAuthorAndSubscriber(roy, frod)
			.get();

		// then
		assertThat(subscription).extracting(
			"id", "author", "subscriber", "notificationEnabled"
		).containsExactly(
			royFrodSubscription.getId(),
			royFrodSubscription.getAuthor(),
			royFrodSubscription.getSubscriber(),
			royFrodSubscription.isNotificationEnabled()
		);
	}

	@DisplayName("구독자의 구독 수를 조회할 수 있다.")
	@Test
	void countBySubscriber() {
		Member frod = createMember("frod", false);
		Member roy = createMember("roy", false);
		Member hana = createMember("hana", false);
		memberRepository.saveAll(asList(frod, roy, hana));

		Subscription royFrodSubscription = createSubscription(roy, frod);
		Subscription hanaFrodSubscription = createSubscription(hana, frod);
		Subscription royHanaSubscription = createSubscription(roy, hana);
		subscriptionRepository.saveAll(asList(royFrodSubscription, hanaFrodSubscription, royHanaSubscription));

		// when
		long subscriptionCount = subscriptionRepository.countBySubscriber(frod);

		// then
		assertThat(subscriptionCount).isEqualTo(2);

	}

	@DisplayName("구독자의 구독 목록을 페이지 단위로 조회할 수 있다.")
	@Test
	void findAllBySubscriberAndAuthorIsDeletedFalse() {
		// given
		Member frod = createMember("frod", false);
		Member roy = createMember("roy", false);
		Member hana = createMember("hana", false);
		memberRepository.saveAll(asList(frod, roy, hana));

		Subscription royFrodSubscription = createSubscription(roy, frod);
		Subscription hanaFrodSubscription = createSubscription(hana, frod);
		Subscription royHanaSubscription = createSubscription(roy, hana);
		subscriptionRepository.saveAll(asList(royFrodSubscription, hanaFrodSubscription, royHanaSubscription));

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
					royFrodSubscription.getId(),
					royFrodSubscription.getAuthor(),
					royFrodSubscription.getSubscriber(),
					royFrodSubscription.isNotificationEnabled()
				),
				tuple(
					hanaFrodSubscription.getId(),
					hanaFrodSubscription.getAuthor(),
					hanaFrodSubscription.getSubscriber(),
					hanaFrodSubscription.isNotificationEnabled()
				)
			);
	}

	@DisplayName("구독자의 구독 목록(페이지 단위) 조회시 탈퇴한 작성자에 대한 구독은 포함되지 않는다.")
	@Test
	void findAllBySubscriberAndAuthorIsDeletedFalse_WithDeletedMember() {
		// given
		Member frod = createMember("frod", false);
		Member roy = createMember("roy", false);
		Member hana = createMember("hana", true);
		memberRepository.saveAll(asList(frod, roy, hana));

		Subscription royFrodSubscription = createSubscription(roy, frod);
		Subscription hanaFrodSubscription = createSubscription(hana, frod);
		Subscription royHanaSubscription = createSubscription(roy, hana);
		subscriptionRepository.saveAll(asList(royFrodSubscription, hanaFrodSubscription, royHanaSubscription));

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
					royFrodSubscription.getId(),
					royFrodSubscription.getAuthor(),
					royFrodSubscription.getSubscriber(),
					royFrodSubscription.isNotificationEnabled()
				)
			);
	}

	@DisplayName("작성자의 구독 목록을 페이지 단위로 조회할 수 있다.")
	@Test
	void findAllByAuthorAndSubscriberIsDeletedFalse() {
		// given
		Member frod = createMember("frod", false);
		Member roy = createMember("roy", false);
		Member hana = createMember("hana", false);
		memberRepository.saveAll(asList(frod, roy, hana));

		Subscription royFrodSubscription = createSubscription(roy, frod);
		Subscription hanaFrodSubscription = createSubscription(hana, frod);
		Subscription royHanaSubscription = createSubscription(roy, hana);
		subscriptionRepository.saveAll(asList(royFrodSubscription, hanaFrodSubscription, royHanaSubscription));

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
					royFrodSubscription.getId(),
					royFrodSubscription.getAuthor(),
					royFrodSubscription.getSubscriber(),
					royFrodSubscription.isNotificationEnabled()
				),
				tuple(
					royHanaSubscription.getId(),
					royHanaSubscription.getAuthor(),
					royHanaSubscription.getSubscriber(),
					royHanaSubscription.isNotificationEnabled()
				)
			);
	}

	@DisplayName("작성자의 구독 목록(페이지 단위) 조회시 탈퇴한 구독자에 대한 구독은 포함되지 않는다.")
	@Test
	void findAllByAuthorAndSubscriberIsDeletedFalse_WithDeletedMember() {
		// given
		Member frod = createMember("frod", false);
		Member roy = createMember("roy", false);
		Member hana = createMember("hana", true);
		memberRepository.saveAll(asList(frod, roy, hana));

		Subscription royFrodSubscription = createSubscription(roy, frod);
		Subscription hanaFrodSubscription = createSubscription(hana, frod);
		Subscription royHanaSubscription = createSubscription(roy, hana);
		subscriptionRepository.saveAll(asList(royFrodSubscription, hanaFrodSubscription, royHanaSubscription));

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
					royFrodSubscription.getId(),
					royFrodSubscription.getAuthor(),
					royFrodSubscription.getSubscriber(),
					royFrodSubscription.isNotificationEnabled()
				)
			);
	}

	@DisplayName("작성자의 구독 목록을 조회할 수 있다.")
	@Test
	void testFindAllByAuthorAndSubscriberIsDeletedFalse() {
		// given
		Member frod = createMember("frod", false);
		Member roy = createMember("roy", false);
		Member hana = createMember("hana", false);
		memberRepository.saveAll(asList(frod, roy, hana));

		Subscription royFrodSubscription = createSubscription(roy, frod);
		Subscription hanaFrodSubscription = createSubscription(hana, frod);
		Subscription royHanaSubscription = createSubscription(roy, hana);
		subscriptionRepository.saveAll(asList(royFrodSubscription, hanaFrodSubscription, royHanaSubscription));

		// when
		List<Subscription> subscriptions = subscriptionRepository
			.findAllByAuthorAndSubscriberIsDeletedFalse(roy);

		// then
		assertThat(subscriptions)
			.hasSize(2)
			.extracting("id", "author", "subscriber", "notificationEnabled")
			.contains(
				tuple(
					royFrodSubscription.getId(),
					royFrodSubscription.getAuthor(),
					royFrodSubscription.getSubscriber(),
					royFrodSubscription.isNotificationEnabled()
				),
				tuple(
					royHanaSubscription.getId(),
					royHanaSubscription.getAuthor(),
					royHanaSubscription.getSubscriber(),
					royHanaSubscription.isNotificationEnabled()
				)
			);
	}

	@DisplayName("작성자의 구독 목록 조회시 탈퇴한 구독자에 대한 구독은 포함되지 않는다.")
	@Test
	void testFindAllByAuthorAndSubscriberIsDeletedFalse_WithDeletedMember() {
		// given
		Member frod = createMember("frod", false);
		Member roy = createMember("roy", false);
		Member hana = createMember("hana", true);
		memberRepository.saveAll(asList(frod, roy, hana));

		Subscription royFrodSubscription = createSubscription(roy, frod);
		Subscription hanaFrodSubscription = createSubscription(hana, frod);
		Subscription royHanaSubscription = createSubscription(roy, hana);
		subscriptionRepository.saveAll(asList(royFrodSubscription, hanaFrodSubscription, royHanaSubscription));

		// when
		List<Subscription> subscriptions = subscriptionRepository
			.findAllByAuthorAndSubscriberIsDeletedFalse(roy);

		// then
		assertThat(subscriptions)
			.hasSize(1)
			.extracting("id", "author", "subscriber", "notificationEnabled")
			.contains(
				tuple(
					royFrodSubscription.getId(),
					royFrodSubscription.getAuthor(),
					royFrodSubscription.getSubscriber(),
					royFrodSubscription.isNotificationEnabled()
				)
			);
	}

	private Member createMember(String userName, boolean isDeleted) {
		return Member.builder()
			.email(userName + "@example.com")
			.username(userName)
			.password(userName + "Password")
			.roles(Set.of(Role.ROLE_USER))
			.platform(Platform.SERVER)
			.createdAt(LocalDateTime.of(2022, 2, 22, 12, 0))
			.updatedAt(LocalDateTime.of(2022, 3, 22, 12, 0))
			.isDeleted(isDeleted)
			.build();
	}

	private Subscription createSubscription(Member author, Member subscriber) {
		return Subscription.builder()
			.id(UUID.randomUUID())
			.author(author)
			.subscriber(subscriber)
			.notificationEnabled(true)
			.createdAt(LocalDateTime.of(2022, 2, 22, 12, 0))
			.build();
	}
}