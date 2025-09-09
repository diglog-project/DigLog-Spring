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

import api.store.diglog.model.constant.Platform;
import api.store.diglog.model.constant.Role;
import api.store.diglog.model.dto.notification.NotificationCreateRequest;
import api.store.diglog.model.dto.notification.NotificationResponse;
import api.store.diglog.model.dto.notification.NotificationUnreadCountResponse;
import api.store.diglog.model.entity.Comment;
import api.store.diglog.model.entity.Member;
import api.store.diglog.model.entity.Post;
import api.store.diglog.model.entity.Subscription;
import api.store.diglog.model.entity.notification.Notification;
import api.store.diglog.repository.CommentRepository;
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
	private CommentRepository commentRepository;

	@Autowired
	private NotificationService notificationService;

	@Autowired
	private SseEmitterRepository sseEmitterRepository;

	@MockitoSpyBean
	private NotificationSubscriber spyNotificationSubscriber;

	@MockitoSpyBean
	private SseEmitterService sseEmitterService;

	private Member loginMember;

	private final List<Member> users = new ArrayList<>();

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

		for (Member user : users) {
			List<SseEmitter> userEmitters = new ArrayList<>(sseEmitterRepository.findById(user.getId()));
			userEmitters.forEach(emitter -> {
				try {
					emitter.complete();
				} catch (Exception ignored) {
				}
				sseEmitterRepository.deleteBy(user.getId(), emitter);
			});
		}

		SecurityContextHolder.clearContext();
		users.clear();

		subscriptionRepository.deleteAllInBatch();
		notificationRepository.deleteAllInBatch();
		commentRepository.deleteAllInBatch();
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

	@Test
	void markAsRead() {
	}

	@Test
	void markAllAsRead() {
	}

	@Test
	void delete() {
	}

	@Test
	void deleteAll() {
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

	private Comment createComment(Post post, Member commenter) {
		return Comment.builder()
			.post(post)
			.member(commenter)
			.content("comment content")
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