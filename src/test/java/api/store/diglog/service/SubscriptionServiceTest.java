package api.store.diglog.service;

import static java.util.Arrays.*;
import static org.assertj.core.api.Assertions.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.IntStream;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import api.store.diglog.common.exception.CustomException;
import api.store.diglog.model.constant.Platform;
import api.store.diglog.model.constant.Role;
import api.store.diglog.model.dto.subscribe.SubscriberResponse;
import api.store.diglog.model.dto.subscribe.SubscriptionCreateRequest;
import api.store.diglog.model.dto.subscribe.SubscriptionCreateResponse;
import api.store.diglog.model.dto.subscribe.SubscriptionExistsResponse;
import api.store.diglog.model.dto.subscribe.SubscriptionNotificationActivationRequest;
import api.store.diglog.model.dto.subscribe.SubscriptionResponse;
import api.store.diglog.model.entity.Member;
import api.store.diglog.model.entity.Subscription;
import api.store.diglog.repository.MemberRepository;
import api.store.diglog.repository.SubscriptionRepository;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class SubscriptionServiceTest {

	@Autowired
	MemberRepository memberRepository;

	@Autowired
	SubscriptionRepository subscriptionRepository;

	@Autowired
	SubscriptionService subscriptionService;

	@BeforeEach
	void setUp() {
		Authentication auth = new UsernamePasswordAuthenticationToken(
			"loginMember@example.com",
			"password",
			List.of(new SimpleGrantedAuthority("ROLE_USER"))
		);
		SecurityContextHolder.getContext().setAuthentication(auth);
	}

	@DisplayName("회원의 구독 목록을 조회할 수 있다.")
	@Test
	void getUserSubscriptions() {
		// given
		Member author = createMember("author");
		Member subscriber = createMember("subscriber");
		memberRepository.saveAll(asList(author, subscriber));

		Subscription subscription = createSubscription(author, subscriber, true);
		subscriptionRepository.save(subscription);

		// when
		Page<SubscriptionResponse> subscriptionResponse =
			subscriptionService.getUserSubscriptions("subscriber", 0, 10);

		// then
		assertThat(subscriptionResponse.getContent())
			.hasSize(1)
			.extracting("subscriptionId", "authorName", "notificationEnabled")
			.containsExactly(tuple(subscription.getId(), author.getUsername(), true));
	}

	@DisplayName("작성자의 구독자 목록을 조회할 수 있다.")
	@Test
	void getAuthorSubscribers() {
		// given
		Member frod = createMember("frod");
		Member roy = createMember("roy");
		Member hana = createMember("hana");
		memberRepository.saveAll(asList(frod, roy, hana));

		Subscription royFrodSubscription = createSubscription(roy, frod, true);
		Subscription hanaFrodSubscription = createSubscription(hana, frod, true);
		Subscription royHanaSubscription = createSubscription(roy, hana, true);
		subscriptionRepository.saveAll(asList(royFrodSubscription, hanaFrodSubscription, royHanaSubscription));

		// when
		Page<SubscriberResponse> subscriptionResponse =
			subscriptionService.getAuthorSubscribers("roy", 0, 10);

		// then
		assertThat(subscriptionResponse.getContent())
			.hasSize(2)
			.extracting("subscriptionId", "subscriberName", "notificationEnabled")
			.containsExactlyInAnyOrderElementsOf(
				asList(
					tuple(royFrodSubscription.getId(), frod.getUsername(), true),
					tuple(royHanaSubscription.getId(), hana.getUsername(), true)
				)
			);
	}

	@DisplayName("로그인한 사용자의 작성자 구독 여부를 확인할 수 있다.")
	@Test
	void checkSubscription() {
		// given
		Member author = createMember("author");
		Member loginMember = createMember("loginMember");
		memberRepository.saveAll(asList(author, loginMember));

		Subscription subscription = createSubscription(author, loginMember, true);
		subscriptionRepository.save(subscription);

		// when
		SubscriptionExistsResponse response = subscriptionService.checkSubscription("author");

		// then
		assertThat(response)
			.extracting("subscriptionId", "hasSubscription")
			.containsExactly(subscription.getId(), true);

	}

	@DisplayName("로그인한 사용자는 작성자를 구독할 수 있다.")
	@Test
	void create() {
		// given
		Member author = createMember("author");
		Member loginMember = createMember("loginMember");
		memberRepository.saveAll(asList(author, loginMember));

		SubscriptionCreateRequest request = SubscriptionCreateRequest.builder()
			.authorName("author")
			.notificationEnabled(true)
			.build();

		// when
		SubscriptionCreateResponse response = subscriptionService.create(request);

		// then
		assertThat(response)
			.extracting("authorName", "subscriberName", "notificationEnabled")
			.containsExactly(author.getUsername(), loginMember.getUsername(), true);
	}

	@DisplayName("자기 자신은 구독할 수 없다.")
	@Test
	void create_WithDeletedMember() {
		// given
		Member loginMember = createMember("loginMember");
		memberRepository.save(loginMember);

		SubscriptionCreateRequest request = SubscriptionCreateRequest.builder()
			.authorName("loginMember")
			.notificationEnabled(true)
			.build();

		// when, then
		assertThatThrownBy(() -> subscriptionService.create(request))
			.isInstanceOf(CustomException.class)
			.hasMessage("자기 자신은 구독할 수 없습니다.");
	}

	@DisplayName("이미 작성자를 구독을 했다면, 새로 구독할 수 없다.")
	@Test
	void create_WithAlreadyExistSubscription() {
		// given
		Member author = createMember("author");
		Member loginMember = createMember("loginMember");
		memberRepository.saveAll(asList(author, loginMember));

		Subscription subscription = createSubscription(author, loginMember, true);
		subscriptionRepository.save(subscription);

		SubscriptionCreateRequest request = SubscriptionCreateRequest.builder()
			.authorName("author")
			.notificationEnabled(true)
			.build();

		// when, then
		assertThatThrownBy(() -> subscriptionService.create(request))
			.isInstanceOf(CustomException.class)
			.hasMessage("이미 해당 작성자를 구독하고 있습니다.");
	}

	@DisplayName("최대 구독 수를 초과할 수 없다.")
	@Test
	void create_WithExceedSubscriptionCount() {
		// given
		List<Member> authors = createMembers(1000);
		Member loginMember = createMember("loginMember");
		memberRepository.saveAll(authors);
		memberRepository.save(loginMember);

		List<Subscription> subscriptions = authors.stream()
			.map(author -> createSubscription(author, loginMember, true))
			.toList();
		subscriptionRepository.saveAll(subscriptions);

		Member exceedAuthor = createMember("exceedAuthor");
		memberRepository.save(exceedAuthor);
		SubscriptionCreateRequest request = SubscriptionCreateRequest.builder()
			.authorName("exceedAuthor")
			.notificationEnabled(true)
			.build();

		// when, then
		assertThatThrownBy(() -> subscriptionService.create(request))
			.isInstanceOf(CustomException.class)
			.hasMessage("구독 가능한 최대 수(1000명)를 초과했습니다.");
	}

	@DisplayName("구독 알림 설정을 변경할 수 있다.")
	@ParameterizedTest(name = "기존={0}, 변경={1}")
	@CsvSource({
		"true, true",
		"true, false",
		"false, true",
		"false, false"
	})
	void updateNotificationSetting(boolean baseNotificationEnabled, boolean changedNotificationEnabled) {
		// given
		Member author = createMember("author");
		Member loginMember = createMember("loginMember");
		memberRepository.saveAll(asList(author, loginMember));

		Subscription subscription = createSubscription(author, loginMember, baseNotificationEnabled);
		subscriptionRepository.save(subscription);

		SubscriptionNotificationActivationRequest request = SubscriptionNotificationActivationRequest.builder()
			.notificationEnabled(changedNotificationEnabled)
			.build();

		// when
		subscriptionService.updateNotificationSetting(subscription.getId(), request);

		// then
		Subscription updatedSubscription = subscriptionRepository.findById(subscription.getId())
			.orElseThrow(() -> new AssertionError("구독이 저장되지 않았습니다."));
		assertThat(updatedSubscription.isNotificationEnabled()).isEqualTo(changedNotificationEnabled);
	}

	@DisplayName("다른 사람의 구독 알림 설정은 변경할 수 없다.")
	@Test
	void updateNotificationSetting_WithInvalidMember() {
		// given
		Member author = createMember("author");
		Member loginMember = createMember("loginMember");
		Member anotherMember = createMember("anotherMember");
		memberRepository.saveAll(asList(author, loginMember, anotherMember));

		Subscription subscription = createSubscription(author, anotherMember, true);
		subscriptionRepository.save(subscription);

		SubscriptionNotificationActivationRequest request = SubscriptionNotificationActivationRequest.builder()
			.notificationEnabled(false)
			.build();

		// when, then
		assertThatThrownBy(() -> subscriptionService.updateNotificationSetting(subscription.getId(), request))
			.isInstanceOf(CustomException.class)
			.hasMessage("구독 수정 권한이 없습니다.");
	}

	@DisplayName("구독을 취소할 수 있다.")
	@Test
	void cancel() {
		// given
		Member author = createMember("author");
		Member loginMember = createMember("loginMember");
		memberRepository.saveAll(asList(author, loginMember));

		Subscription subscription = createSubscription(author, loginMember, true);
		subscriptionRepository.save(subscription);

		// when
		subscriptionService.cancel(subscription.getId());

		// then
		assertThat(subscriptionRepository.existsById(subscription.getId())).isFalse();
	}

	@DisplayName("다른 사람의 구독을 취소할 수 없다.")
	@Test
	void cancel_WithInvalidMember() {
		// given
		Member author = createMember("author");
		Member loginMember = createMember("loginMember");
		Member anotherMember = createMember("anotherMember");
		memberRepository.saveAll(asList(author, loginMember, anotherMember));

		Subscription subscription = createSubscription(author, anotherMember, true);
		subscriptionRepository.save(subscription);

		// when, then
		assertThatThrownBy(() -> subscriptionService.cancel(subscription.getId()))
			.isInstanceOf(CustomException.class)
			.hasMessage("구독 수정 권한이 없습니다.");
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

	private List<Member> createMembers(int size) {
		return IntStream.range(0, size)
			.boxed()
			.map(count -> Member.builder()
				.email("user" + count + "@example.com")
				.username("user" + count)
				.password("user" + count + "Password")
				.roles(Set.of(Role.ROLE_USER))
				.platform(Platform.SERVER)
				.createdAt(LocalDateTime.of(2000 + count, 2, 22, 12, 0))
				.updatedAt(LocalDateTime.of(2000 + count, 3, 22, 12, 0))
				.isDeleted(false)
				.build())
			.toList();
	}

	private Subscription createSubscription(Member author, Member subscriber, boolean notificationEnabled) {
		return Subscription.builder()
			.id(UUID.randomUUID())
			.author(author)
			.subscriber(subscriber)
			.notificationEnabled(notificationEnabled)
			.createdAt(LocalDateTime.of(2022, 2, 22, 12, 0))
			.build();
	}
}