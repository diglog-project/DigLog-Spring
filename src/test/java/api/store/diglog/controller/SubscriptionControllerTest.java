package api.store.diglog.controller;

import static java.util.Arrays.*;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.BDDMockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;

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
import api.store.diglog.service.SubscriptionService;

@WebMvcTest(controllers = SubscriptionController.class)
class SubscriptionControllerTest {

	private static final String EMAIL = "loginMember@example.com";
	private static final String PASSWORD = "loginMemberPassword";

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private ObjectMapper objectMapper;

	@MockitoBean
	private SubscriptionService subscriptionService;

	@DisplayName("사용자의 구독 목록을 조회할 수 있다.")
	@Test
	@WithMockUser(username = EMAIL, password = PASSWORD)
	void getUserSubscriptions() throws Exception {
		// given
		Member author = createMember("author");
		Member subscriber = createMember("subscriber");
		Subscription subscription = createSubscription(author, subscriber, true);

		SubscriptionResponse response = SubscriptionResponse.from(subscription);
		Page<SubscriptionResponse> page = new PageImpl<>(List.of(response));

		BDDMockito.doReturn(page)
			.when(subscriptionService)
			.getUserSubscriptions(anyString(), anyInt(), anyInt());

		// when, then
		mockMvc.perform(get("/api/subscriptions/users/{username}", "subscriber")
				.param("page", "0")
				.param("size", "20")
				.contentType(MediaType.APPLICATION_JSON)
				.accept(MediaType.APPLICATION_JSON))
			.andDo(print())
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.content[0].subscriptionId").value(subscription.getId().toString()))
			.andExpect(jsonPath("$.content[0].authorName").value("author"))
			.andExpect(jsonPath("$.content[0].notificationEnabled").value(true));
	}

	@DisplayName("작성자의 구독자 목록을 조회할 수 있다.")
	@Test
	@WithMockUser(username = EMAIL, password = PASSWORD)
	void getAuthorSubscribers() throws Exception {
		// given
		Member author = createMember("author");
		Member subscriber01 = createMember("subscriber01");
		Member subscriber02 = createMember("subscriber02");
		Subscription subscription01 = createSubscription(author, subscriber01, true);
		Subscription subscription02 = createSubscription(author, subscriber02, true);

		List<SubscriberResponse> responses = asList(
			SubscriberResponse.from(subscription01),
			SubscriberResponse.from(subscription02)
		);
		Page<SubscriberResponse> page = new PageImpl<>(responses);

		BDDMockito.doReturn(page)
			.when(subscriptionService)
			.getAuthorSubscribers(anyString(), anyInt(), anyInt());

		// when, then
		mockMvc.perform(get("/api/subscriptions/authors/{authorName}", "author")
				.param("page", "0")
				.param("size", "20")
				.contentType(MediaType.APPLICATION_JSON)
				.accept(MediaType.APPLICATION_JSON))
			.andDo(print())
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.content[0].subscriptionId").value(subscription01.getId().toString()))
			.andExpect(jsonPath("$.content[0].subscriberName").value(subscription01.getSubscriber().getUsername()))
			.andExpect(jsonPath("$.content[0].notificationEnabled").value(true))
			.andExpect(jsonPath("$.content[1].subscriptionId").value(subscription02.getId().toString()))
			.andExpect(jsonPath("$.content[1].subscriberName").value(subscription02.getSubscriber().getUsername()))
			.andExpect(jsonPath("$.content[1].notificationEnabled").value(true));
	}

	@DisplayName("로그인한 사용자의 작성자 구독 여부를 확인할 수 있다.")
	@Test
	@WithMockUser(username = EMAIL, password = PASSWORD)
	void checkSubscription() throws Exception {
		// given
		Member author = createMember("author");
		Member loginMember = createMember("loginMember");
		Subscription subscription = createSubscription(author, loginMember, true);

		SubscriptionExistsResponse response = SubscriptionExistsResponse.of(subscription.getId(), true);

		BDDMockito.doReturn(response)
			.when(subscriptionService)
			.checkSubscription(anyString());

		// when, then
		mockMvc.perform(get("/api/subscriptions/current-member/authors/{authorName}", "author")
				.with(csrf())
				.contentType(MediaType.APPLICATION_JSON)
				.accept(MediaType.APPLICATION_JSON))
			.andDo(print())
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.subscriptionId").value(subscription.getId().toString()))
			.andExpect(jsonPath("$.hasSubscription").value(true));
	}

	@DisplayName("로그인한 사용자는 작성자를 구독할 수 있다.")
	@Test
	@WithMockUser(username = EMAIL, password = PASSWORD)
	void create() throws Exception {
		// given
		SubscriptionCreateRequest request = SubscriptionCreateRequest.builder()
			.authorName("author")
			.notificationEnabled(true)
			.build();

		Member author = createMember("author");
		Member loginMember = createMember("loginMember");
		Subscription subscription = createSubscription(author, loginMember, true);

		SubscriptionCreateResponse response = SubscriptionCreateResponse.from(subscription);

		BDDMockito.doReturn(response)
			.when(subscriptionService)
			.create(any());

		// when, then
		mockMvc.perform(post("/api/subscriptions")
				.with(csrf())
				.content(objectMapper.writeValueAsString(request))
				.contentType(MediaType.APPLICATION_JSON)
				.accept(MediaType.APPLICATION_JSON))
			.andDo(print())
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.authorName").value("author"))
			.andExpect(jsonPath("$.subscriberName").value("loginMember"))
			.andExpect(jsonPath("$.notificationEnabled").value(true))
			.andExpect(jsonPath("$.createdAt")
				.value(subscription.getCreatedAt()
					.format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss"))));
	}

	@DisplayName("구독 알림 설정을 변경할 수 있다.")
	@Test
	@WithMockUser(username = EMAIL, password = PASSWORD)
	void updateNotificationSetting() throws Exception {
		// given
		SubscriptionNotificationActivationRequest request = SubscriptionNotificationActivationRequest.builder()
			.notificationEnabled(true)
			.build();

		Member author = createMember("author");
		Member loginMember = createMember("loginMember");
		Subscription subscription = createSubscription(author, loginMember, true);

		BDDMockito.doNothing()
			.when(subscriptionService)
			.updateNotificationSetting(any(), any());

		// when, then
		mockMvc.perform(
				patch("/api/subscriptions/{subscriptionId}/notification-setting", subscription.getId().toString())
					.with(csrf())
					.content(objectMapper.writeValueAsString(request))
					.contentType(MediaType.APPLICATION_JSON)
					.accept(MediaType.APPLICATION_JSON))
			.andDo(print())
			.andExpect(status().isNoContent());
	}

	@DisplayName("구독을 취소할 수 있다.")
	@Test
	@WithMockUser(username = EMAIL, password = PASSWORD)
	void cancel() throws Exception {
		// given
		Member author = createMember("author");
		Member loginMember = createMember("loginMember");
		Subscription subscription = createSubscription(author, loginMember, true);

		BDDMockito.doNothing()
			.when(subscriptionService)
			.cancel(any());

		// when, then
		mockMvc.perform(
				delete("/api/subscriptions/{subscriptionId}", subscription.getId().toString())
					.with(csrf())
					.contentType(MediaType.APPLICATION_JSON)
					.accept(MediaType.APPLICATION_JSON))
			.andDo(print())
			.andExpect(status().isNoContent());

	}

	private Member createMember(String userName) {
		return Member.builder()
			.email(userName + "@example.com")
			.username(userName)
			.password(userName + "Password")
			.roles(Set.of(Role.ROLE_USER))
			.platform(Platform.SERVER)
			.createdAt(LocalDateTime.of(2022, 2, 22, 12, 0, 0, 0))
			.updatedAt(LocalDateTime.of(2022, 3, 22, 12, 0, 0, 0))
			.isDeleted(false)
			.build();
	}

	private Subscription createSubscription(Member author, Member subscriber, boolean notificationEnabled) {
		return Subscription.builder()
			.id(UUID.randomUUID())
			.author(author)
			.subscriber(subscriber)
			.notificationEnabled(notificationEnabled)
			.createdAt(LocalDateTime.of(2022, 2, 22, 12, 0, 0, 0))
			.build();
	}

}