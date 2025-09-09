package api.store.diglog.controller;

import static api.store.diglog.model.entity.notification.NotificationType.*;
import static org.hamcrest.Matchers.*;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.time.LocalDateTime;
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
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import com.fasterxml.jackson.databind.ObjectMapper;

import api.store.diglog.model.constant.Platform;
import api.store.diglog.model.constant.Role;
import api.store.diglog.model.dto.notification.NotificationCreateRequest;
import api.store.diglog.model.dto.notification.NotificationDeleteRequest;
import api.store.diglog.model.dto.notification.NotificationDeleteResponse;
import api.store.diglog.model.dto.notification.NotificationReadResponse;
import api.store.diglog.model.dto.notification.NotificationResponse;
import api.store.diglog.model.dto.notification.NotificationUnreadCountResponse;
import api.store.diglog.model.entity.Member;
import api.store.diglog.model.entity.notification.Notification;
import api.store.diglog.model.entity.notification.NotificationType;
import api.store.diglog.service.SseEmitterService;
import api.store.diglog.service.notification.NotificationService;

@WebMvcTest(controllers = NotificationController.class)
class NotificationControllerTest {
	private static final String EMAIL = "loginMember@example.com";
	private static final String PASSWORD = "loginMemberPassword";

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private ObjectMapper objectMapper;

	@MockitoBean
	private NotificationService notificationService;

	@MockitoBean
	private SseEmitterService sseEmitterService;

	@DisplayName("SSE 구독을 할 수 있다.")
	@Test
	@WithMockUser(username = EMAIL, password = PASSWORD)
	void subscribe() throws Exception {
		// given
		SseEmitter sseEmitter = new SseEmitter();

		BDDMockito.doReturn(sseEmitter)
			.when(sseEmitterService)
			.subscribe();

		// when, then
		mockMvc.perform(get("/api/notifications/sse/subscribe")
				.with(csrf())
				.accept(MediaType.TEXT_EVENT_STREAM_VALUE))
			.andExpect(status().isOk());

		verify(sseEmitterService).subscribe();
	}

	@DisplayName("알림 목록을 조회할 수 있다.")
	@Test
	@WithMockUser(username = EMAIL, password = PASSWORD)
	void search() throws Exception {
		// given
		Member loginMember = createMember("loginMember");
		Notification notification = createNotification(loginMember, POST_CREATION, false);
		NotificationResponse response = NotificationResponse.from(notification);

		Page<NotificationResponse> page = new PageImpl<>(List.of(response));

		BDDMockito.doReturn(page)
			.when(notificationService)
			.searchBy(anyInt(), anyInt());

		// when, then
		mockMvc.perform(get("/api/notifications")
				.param("page", "0")
				.param("size", "20")
				.contentType(MediaType.APPLICATION_JSON)
				.accept(MediaType.APPLICATION_JSON))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.content[0].notificationId").value(notification.getId().toString()))
			.andExpect(jsonPath("$.content[0].notificationType").value("POST_CREATION"))
			.andExpect(jsonPath("$.content[0].message").value("알림 생성"))
			.andExpect(jsonPath("$.content[0].isRead").value(false));

		verify(notificationService).searchBy(0, 20);
	}

	@DisplayName("페이지 파라미터가 음수인 경우 유효성 검증에 실패한다.")
	@Test
	@WithMockUser(username = EMAIL, password = PASSWORD)
	void search_InvalidPageParam() throws Exception {
		// when, then
		mockMvc.perform(get("/api/notifications")
				.param("page", "-1")
				.param("size", "20")
				.contentType(MediaType.APPLICATION_JSON)
				.accept(MediaType.APPLICATION_JSON))
			.andExpect(status().isBadRequest());
	}

	@DisplayName("읽지 않은 알림 개수를 조회할 수 있다.")
	@Test
	@WithMockUser(username = EMAIL, password = PASSWORD)
	void countUnreadNotification() throws Exception {
		// given
		NotificationUnreadCountResponse response = NotificationUnreadCountResponse.of(5);

		BDDMockito.doReturn(response)
			.when(notificationService)
			.countUnreadNotification();

		// when, then
		mockMvc.perform(get("/api/notifications/unread-count")
				.contentType(MediaType.APPLICATION_JSON)
				.accept(MediaType.APPLICATION_JSON))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.unreadCount").value(5));
	}

	@DisplayName("새로운 알림을 생성할 수 있다.")
	@Test
	@WithMockUser(username = EMAIL, password = PASSWORD)
	void create() throws Exception {
		// given
		NotificationCreateRequest request = NotificationCreateRequest.builder()
			.notificationType("POST_CREATION")
			.dataId(UUID.randomUUID())
			.build();

		BDDMockito.doNothing()
			.when(notificationService)
			.createAndPublish(any());

		// when, then
		mockMvc.perform(post("/api/notifications")
				.with(csrf())
				.content(objectMapper.writeValueAsString(request))
				.contentType(MediaType.APPLICATION_JSON)
				.accept(MediaType.APPLICATION_JSON))
			.andExpect(status().isNoContent());

		verify(notificationService).createAndPublish(any(NotificationCreateRequest.class));
	}

	@DisplayName("유효하지 않은 요청으로 알림 생성 시 실패한다.")
	@Test
	@WithMockUser(username = EMAIL, password = PASSWORD)
	void create_InvalidRequest() throws Exception {
		// given
		NotificationCreateRequest request = NotificationCreateRequest.builder()
			.notificationType("  ")
			.dataId(UUID.randomUUID())
			.build();

		// when, then
		mockMvc.perform(post("/api/notifications")
				.with(csrf())
				.content(objectMapper.writeValueAsString(request))
				.contentType(MediaType.APPLICATION_JSON)
				.accept(MediaType.APPLICATION_JSON))
			.andExpect(status().isBadRequest());
	}

	@DisplayName("로그인 사용자의 특정 알림을 읽음 처리할 수 있다.")
	@Test
	@WithMockUser(username = EMAIL, password = PASSWORD)
	void markAsRead() throws Exception {
		// given
		Member loginMember = createMember("loginMember");
		Notification notification = createNotification(loginMember, POST_CREATION, true);
		NotificationReadResponse response = NotificationReadResponse.from(notification);

		BDDMockito.doReturn(response)
			.when(notificationService)
			.markAsRead(notification.getId());

		// when, then
		mockMvc.perform(patch("/api/notifications/{notificationId}/read", notification.getId())
				.with(csrf())
				.contentType(MediaType.APPLICATION_JSON)
				.accept(MediaType.APPLICATION_JSON))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.notificationId").value(notification.getId().toString()))
			.andExpect(jsonPath("$.isRead").value(true));
	}

	@DisplayName("로그인 사용자의 모든 알림을 읽음 처리할 수 있다.")
	@Test
	@WithMockUser(username = EMAIL, password = PASSWORD)
	void markAllAsRead() throws Exception {
		// given
		Member loginMember = createMember("loginMember");
		Notification postCreationNotification = createNotification(loginMember, POST_CREATION, true);
		Notification commentCreationNotification = createNotification(loginMember, COMMENT_CREATION, true);

		List<NotificationReadResponse> responses = List.of(
			NotificationReadResponse.from(postCreationNotification),
			NotificationReadResponse.from(commentCreationNotification)
		);

		BDDMockito.doReturn(responses)
			.when(notificationService)
			.markAllAsRead();

		// when, then
		mockMvc.perform(patch("/api/notifications/read-all")
				.with(csrf())
				.contentType(MediaType.APPLICATION_JSON)
				.accept(MediaType.APPLICATION_JSON))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$", hasSize(2)))
			.andExpect(jsonPath("$[0].notificationId").value(postCreationNotification.getId().toString()))
			.andExpect(jsonPath("$[0].isRead").value(true))
			.andExpect(jsonPath("$[1].notificationId").value(commentCreationNotification.getId().toString()))
			.andExpect(jsonPath("$[1].isRead").value(true));
	}

	@DisplayName("로그인 사용자의 특정 알림을 삭제할 수 있다.")
	@Test
	@WithMockUser(username = EMAIL, password = PASSWORD)
	void delete_LoginMember() throws Exception {
		// given
		UUID notificationId = UUID.randomUUID();

		BDDMockito.doNothing()
			.when(notificationService)
			.delete(notificationId);

		// when, then
		mockMvc.perform(delete("/api/notifications/{notificationId}", notificationId)
				.with(csrf())
				.contentType(MediaType.APPLICATION_JSON)
				.accept(MediaType.APPLICATION_JSON))
			.andExpect(status().isNoContent());

		verify(notificationService).delete(notificationId);
	}

	@DisplayName("여러 알림을 일괄 삭제할 수 있다.")
	@Test
	@WithMockUser(username = EMAIL, password = PASSWORD)
	void deleteAll() throws Exception {
		// given
		Set<UUID> notificationIds = Set.of(UUID.randomUUID(), UUID.randomUUID());

		NotificationDeleteRequest request = NotificationDeleteRequest.builder()
			.notificationIds(notificationIds)
			.build();

		NotificationDeleteResponse response = NotificationDeleteResponse.of(2, 2);

		BDDMockito.doReturn(response)
			.when(notificationService)
			.deleteAll(any());

		// when, then
		mockMvc.perform(delete("/api/notifications")
				.with(csrf())
				.content(objectMapper.writeValueAsString(request))
				.contentType(MediaType.APPLICATION_JSON)
				.accept(MediaType.APPLICATION_JSON))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.requestedCount").value(2))
			.andExpect(jsonPath("$.deletedCount").value(2));

		verify(notificationService).deleteAll(any(NotificationDeleteRequest.class));
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

	private Notification createNotification(Member receiver, NotificationType notificationType, boolean isRead) {
		return Notification.builder()
			.id(UUID.randomUUID())
			.receiver(receiver)
			.notificationType(notificationType)
			.message("알림 생성")
			.isRead(isRead)
			.build();
	}
}