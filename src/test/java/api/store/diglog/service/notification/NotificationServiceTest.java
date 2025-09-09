package api.store.diglog.service.notification;

import static api.store.diglog.model.entity.notification.NotificationType.*;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.testcontainers.shaded.org.awaitility.Awaitility.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import api.store.diglog.common.exception.CustomException;
import api.store.diglog.model.constant.Platform;
import api.store.diglog.model.constant.Role;
import api.store.diglog.model.dto.notification.NotificationCreateRequest;
import api.store.diglog.model.dto.notification.NotificationDeleteRequest;
import api.store.diglog.model.dto.notification.NotificationResponse;
import api.store.diglog.model.dto.notification.NotificationUnreadCountResponse;
import api.store.diglog.model.entity.Member;
import api.store.diglog.model.entity.Post;
import api.store.diglog.model.entity.Subscription;
import api.store.diglog.model.entity.notification.Notification;
import api.store.diglog.repository.MemberRepository;
import api.store.diglog.repository.NotificationRepository;
import api.store.diglog.repository.PostRepository;
import api.store.diglog.repository.SseEmitterRepository;
import api.store.diglog.repository.SubscriptionRepository;
import api.store.diglog.service.SseEmitterService;
import api.store.diglog.supporter.RedisTestSupporter;

@SpringBootTest
@ActiveProfiles("test")
class NotificationServiceTest extends RedisTestSupporter {

	@Autowired
	private MemberRepository memberRepository;

	@Autowired
	private NotificationRepository notificationRepository;

	@Autowired
	private SubscriptionRepository subscriptionRepository;

	@Autowired
	private PostRepository postRepository;

	@Autowired
	private NotificationService notificationService;

	@Autowired
	private SseEmitterRepository sseEmitterRepository;

	@MockitoSpyBean
	private NotificationSubscriber spyNotificationSubscriber;

	@MockitoSpyBean
	private SseEmitterService sseEmitterService;

	private Member loginMember;

	@BeforeEach
	void setUp() {
		Authentication auth = new UsernamePasswordAuthenticationToken(
			"loginMember@example.com",
			"password",
			List.of(new SimpleGrantedAuthority("ROLE_USER"))
		);
		SecurityContextHolder.getContext().setAuthentication(auth);

		loginMember = createMember("loginMember");
		memberRepository.save(loginMember);
	}

	@AfterEach
	void tearDown() {
		List<SseEmitter> emittersToCleanup = new ArrayList<>(sseEmitterRepository.findById(loginMember.getId()));
		emittersToCleanup.forEach(emitter -> {
			try {
				emitter.complete();
			} catch (Exception ignored) {
			}
			sseEmitterRepository.deleteBy(loginMember.getId(), emitter);
		});

		SecurityContextHolder.clearContext();

		subscriptionRepository.deleteAllInBatch();
		notificationRepository.deleteAllInBatch();
		postRepository.deleteAllInBatch();
		memberRepository.deleteAllInBatch();
	}

	@DisplayName("알림 생성 후 전송할 수 있다")
	@Test
	void createAndPublish() {
		// Given
		Member author = createMember("author");
		Member receiver = createMember("receiver");
		memberRepository.saveAll(List.of(author, receiver));

		Subscription subscription = createSubscription(author, receiver, true);
		subscriptionRepository.save(subscription);

		Post post = createPost(author);
		postRepository.save(post);

		sseEmitterRepository.save(loginMember.getId(), new SseEmitter());

		NotificationCreateRequest request = NotificationCreateRequest.builder()
			.notificationType("POST_CREATION")
			.dataId(post.getId())
			.build();

		// When
		notificationService.createAndPublish(request);

		// Then
		await().atMost(5, TimeUnit.SECONDS).untilAsserted(() -> {
			verify(spyNotificationSubscriber, times(1)).onMessage(any(), any());
			verify(sseEmitterService, times(1)).send(
				eq(receiver.getId()),
				eq(author.getUsername() + "님이 \"" + post.getTitle() + "\" 게시글을 작성했습니다.")
			);
		});
	}

	@DisplayName("로그인 사용자의 알림 목록(페이지 단위)을 조회할 수 있다.")
	@Test
	void searchBy() {
		// Given
		List<Notification> notifications = List.of(
			createCommentCreationNotification(loginMember, false),
			createCommentCreationNotification(loginMember, false),
			createCommentCreationNotification(loginMember, false)
		);
		List<Notification> savedNotifications = notificationRepository.saveAll(notifications);

		// When
		Page<NotificationResponse> notificationResponses = notificationService.searchBy(0, 20);

		// Then
		assertThat(notificationResponses.getContent()).hasSize(3)
			.extracting("notificationId", "notificationType", "message")
			.containsExactlyInAnyOrder(
				tuple(savedNotifications.get(0).getId(), COMMENT_CREATION, "댓글 생성 알림"),
				tuple(savedNotifications.get(1).getId(), COMMENT_CREATION, "댓글 생성 알림"),
				tuple(savedNotifications.get(2).getId(), COMMENT_CREATION, "댓글 생성 알림")
			);
	}

	@DisplayName("로그인 사용자의 읽지 않은 알림 개수를 집계할 수 있다")
	@Test
	void countUnreadNotification() {
		// Given
		List<Notification> notifications = List.of(
			createCommentCreationNotification(loginMember, false),
			createCommentCreationNotification(loginMember, true),
			createCommentCreationNotification(loginMember, false)
		);
		notificationRepository.saveAll(notifications);

		// When
		NotificationUnreadCountResponse response = notificationService.countUnreadNotification();

		// Then
		assertThat(response.getUnreadCount()).isEqualTo(2);
	}

	@DisplayName("로그인 사용자의 알림을 읽음 처리 할 수 있다")
	@Test
	void markAsRead() {
		// Given
		Notification notification = createCommentCreationNotification(loginMember, false);
		notificationRepository.save(notification);

		// When
		notificationService.markAsRead(notification.getId());

		// Then
		Notification updatedNotification = notificationRepository.findById(notification.getId()).orElseThrow();
		assertThat(updatedNotification)
			.extracting("id", "receiver.id", "isRead")
			.containsExactly(notification.getId(), loginMember.getId(), true);
	}

	@DisplayName("다른 사용자의 알림을 읽음 처리를 할 수 없다")
	@Test
	void markAsRead_InvalidLoginMember() {
		// Given
		Member anotherMember = createMember("anotherMember");
		memberRepository.save(anotherMember);

		Notification notification = createCommentCreationNotification(anotherMember, false);
		notificationRepository.save(notification);

		// When, Then
		assertThatThrownBy(() -> notificationService.markAsRead(notification.getId()))
			.isInstanceOf(CustomException.class)
			.hasMessage("해당 알림의 변경 권한이 없습니다.");
	}

	@DisplayName("로그인 사용자의 알림을 일괄적으로 읽음 처리 할 수 있다")
	@Test
	void markAllAsRead() {
		// Given
		Notification commentNotification = createCommentCreationNotification(loginMember, false);
		Notification postCreationNotification01 = createPostCreationNotification(loginMember, true);
		Notification postCreationNotification02 = createPostCreationNotification(loginMember, false);
		notificationRepository.saveAll(
			List.of(commentNotification, postCreationNotification01, postCreationNotification02)
		);

		// When
		notificationService.markAllAsRead();

		// Then
		List<Notification> updatedNotifications = notificationRepository.findAll();
		assertThat(updatedNotifications).hasSize(3)
			.extracting("id", "receiver.id", "isRead")
			.containsExactlyInAnyOrder(
				tuple(commentNotification.getId(), loginMember.getId(), true),
				tuple(postCreationNotification01.getId(), loginMember.getId(), true),
				tuple(postCreationNotification02.getId(), loginMember.getId(), true)
			);
	}

	@DisplayName("로그인 사용자의 알림을 삭제할 수 있다")
	@Test
	void delete() {
		// Given
		Notification notification = createCommentCreationNotification(loginMember, false);
		notificationRepository.save(notification);

		// When
		notificationService.delete(notification.getId());

		// Then
		boolean exists = notificationRepository.existsById(notification.getId());
		assertThat(exists).isFalse();
	}

	@DisplayName("로그인 사용자는 다른 사용자의 알림을 삭제할 수 없다")
	@Test
	void delete_InvalidLoginMember() {
		// Given
		Member anotherMember = createMember("anotherMember");
		memberRepository.save(anotherMember);

		Notification notification = createCommentCreationNotification(anotherMember, false);
		notificationRepository.save(notification);

		// When, Then
		assertThatThrownBy(() -> notificationService.delete(notification.getId()))
			.isInstanceOf(CustomException.class)
			.hasMessage("해당 알림의 변경 권한이 없습니다.");
	}

	@DisplayName("로그인 사용자의 다건 알림을 삭제할 수 있다")
	@Test
	void deleteAll() {
		// Given
		List<Notification> notifications = List.of(
			createCommentCreationNotification(loginMember, false),
			createPostCreationNotification(loginMember, true),
			createPostCreationNotification(loginMember, true)
		);
		notificationRepository.saveAll(notifications);

		Set<UUID> ids = notifications.stream()
			.map(Notification::getId)
			.collect(Collectors.toSet());
		NotificationDeleteRequest request = NotificationDeleteRequest.builder()
			.notificationIds(ids)
			.build();

		// When
		notificationService.deleteAll(request);

		// Then
		List<Notification> deletedNotifications = notificationRepository.findAllById(request.getNotificationIds());
		assertThat(deletedNotifications).isEmpty();
	}

	@DisplayName("다건 알림 삭제 중 다른 사람의 알림이 섞여 있을 경우, 요청한 모든 알림은 삭제되지 않는다")
	@Test
	void deleteAll_ContainsOtherNotification() {
		// Given
		Member anotherMember = createMember("anotherMember");
		memberRepository.save(anotherMember);

		List<Notification> notifications = List.of(
			createCommentCreationNotification(loginMember, false),
			createPostCreationNotification(loginMember, true),
			createPostCreationNotification(anotherMember, true)
		);
		notificationRepository.saveAll(notifications);

		Set<UUID> ids = notifications.stream()
			.map(Notification::getId)
			.collect(Collectors.toSet());
		NotificationDeleteRequest request = NotificationDeleteRequest.builder()
			.notificationIds(ids)
			.build();

		// When, Then
		assertThatThrownBy(() -> notificationService.deleteAll(request))
			.isInstanceOf(CustomException.class)
			.hasMessage("해당 알림의 변경 권한이 없습니다.");

		List<Notification> allNotification = notificationRepository.findAll();
		assertThat(allNotification).hasSize(3)
			.extracting("id", "receiver.id", "notificationType", "isRead")
			.containsExactlyInAnyOrder(
				tuple(notifications.getFirst().getId(), loginMember.getId(), COMMENT_CREATION, false),
				tuple(notifications.get(1).getId(), loginMember.getId(), POST_CREATION, true),
				tuple(notifications.get(2).getId(), anotherMember.getId(), POST_CREATION, true)
			);
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

	private Post createPost(Member author) {
		return Post.builder()
			.member(author)
			.title("post title")
			.content("post content")
			.build();
	}

	private Subscription createSubscription(Member author, Member subscriber, boolean notificationEnabled) {
		return Subscription.builder()
			.id(UUID.randomUUID())
			.author(author)
			.subscriber(subscriber)
			.notificationEnabled(notificationEnabled)
			.build();

	}

	private Notification createCommentCreationNotification(Member receiver, boolean isRead) {
		return Notification.builder()
			.id(UUID.randomUUID())
			.receiver(receiver)
			.notificationType(COMMENT_CREATION)
			.message("댓글 생성 알림")
			.isRead(isRead)
			.build();
	}

	private Notification createPostCreationNotification(Member receiver, boolean isRead) {
		return Notification.builder()
			.id(UUID.randomUUID())
			.receiver(receiver)
			.notificationType(POST_CREATION)
			.message("게시글 생성 알림")
			.isRead(isRead)
			.build();
	}
}