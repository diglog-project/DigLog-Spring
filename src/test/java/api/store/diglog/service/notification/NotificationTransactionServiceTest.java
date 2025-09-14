package api.store.diglog.service.notification;

import static api.store.diglog.model.entity.notification.NotificationType.*;
import static org.assertj.core.api.Assertions.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.transaction.annotation.Transactional;

import api.store.diglog.common.exception.CustomException;
import api.store.diglog.common.exception.ErrorCode;
import api.store.diglog.model.constant.Platform;
import api.store.diglog.model.constant.Role;
import api.store.diglog.model.dto.notification.NotificationCreateRequest;
import api.store.diglog.model.dto.notification.NotificationDeleteRequest;
import api.store.diglog.model.entity.Comment;
import api.store.diglog.model.entity.Member;
import api.store.diglog.model.entity.Post;
import api.store.diglog.model.entity.Subscription;
import api.store.diglog.model.entity.notification.Notification;
import api.store.diglog.supporter.IntegrationTestSupport;

@Transactional
class NotificationTransactionServiceTest extends IntegrationTestSupport {

	private Member loginMember;

	@BeforeEach
	void setUp() {
		Authentication auth = new UsernamePasswordAuthenticationToken(
			"loginMember@example.com",
			"password",
			List.of(new SimpleGrantedAuthority("ROLE_USER"))
		);
		SecurityContextHolder.getContext().setAuthentication(auth);

		Member member = createMember("loginMember");
		loginMember = memberRepository.save(member);
	}

	@AfterEach
	void tearDown() {
		SecurityContextHolder.clearContext();
	}

	@DisplayName("게시글 생성 알림을 만들 수 있다")
	@Test
	void create_PostCreationNotification() {
		// Given
		Member author = createMember("author");
		Member receiver = createMember("receiver");
		memberRepository.saveAll(List.of(author, receiver));

		Subscription subscription = createSubscription(author, receiver, true);
		subscriptionRepository.save(subscription);

		Post post = createPost(author);
		postRepository.save(post);

		NotificationCreateRequest request = NotificationCreateRequest.builder()
			.notificationType("POST_CREATION")
			.dataId(post.getId())
			.build();

		// When
		List<Notification> notifications = notificationTransactionService.create(request);

		// Then
		assertThat(notifications).hasSize(1)
			.extracting("id", "receiver.id", "notificationType", "message", "isRead")
			.containsExactly(tuple(
				notifications.getFirst().getId(),
				receiver.getId(),
				POST_CREATION,
				author.getUsername() + "님이 \"" + post.getTitle() + "\" 게시글을 작성했습니다.",
				false)
			);
	}

	@DisplayName("구독에서 알림 여부가 비활성화 되어 있다면, 게시글 생성 알림이 만들어지지 않는다")
	@Test
	void create_PostCreationNotification_SubscriptionNotificationEnabledFalse() {
		// Given
		Member author = createMember("author");
		Member receiver = createMember("receiver");
		memberRepository.saveAll(List.of(author, receiver));

		Subscription subscription = createSubscription(author, receiver, false);
		subscriptionRepository.save(subscription);

		Post post = createPost(author);
		postRepository.save(post);

		NotificationCreateRequest request = NotificationCreateRequest.builder()
			.notificationType("POST_CREATION")
			.dataId(post.getId())
			.build();

		// When
		List<Notification> notifications = notificationTransactionService.create(request);

		// Then
		assertThat(notifications).isEmpty();
	}

	@DisplayName("알림 설정을 활성화한 구독자 수만큼 알림이 생성된다")
	@Test
	void create_PostCreationNotifications() {
		// Given
		Member author = createMember("author");
		Member receiver01 = createMember("receiver01");
		Member receiver02 = createMember("receiver02");
		Member receiver03 = createMember("receiver03");
		memberRepository.saveAll(List.of(author, receiver01, receiver02, receiver03));

		Subscription subscription01 = createSubscription(author, receiver01, true);
		Subscription subscription02 = createSubscription(author, receiver02, true);
		Subscription subscription03 = createSubscription(author, receiver03, true);
		subscriptionRepository.saveAll(List.of(subscription01, subscription02, subscription03));

		Post post = createPost(author);
		postRepository.save(post);

		NotificationCreateRequest request = NotificationCreateRequest.builder()
			.notificationType("POST_CREATION")
			.dataId(post.getId())
			.build();

		// When
		List<Notification> notifications = notificationTransactionService.create(request);

		// Then
		assertThat(notifications).hasSize(3)
			.extracting("receiver.id", "notificationType", "isRead", "message")
			.containsExactlyInAnyOrder(
				tuple(receiver01.getId(), POST_CREATION, false,
					author.getUsername() + "님이 \"" + post.getTitle() + "\" 게시글을 작성했습니다."),
				tuple(receiver02.getId(), POST_CREATION, false,
					author.getUsername() + "님이 \"" + post.getTitle() + "\" 게시글을 작성했습니다."),
				tuple(receiver03.getId(), POST_CREATION, false,
					author.getUsername() + "님이 \"" + post.getTitle() + "\" 게시글을 작성했습니다.")
			);
	}

	@DisplayName("댓글 생성 알림을 만들 수 있다")
	@Test
	void create_CommentCreationNotification() {
		// Given
		Member author = createMember("author");
		Member commenter = createMember("commenter");
		memberRepository.saveAll(List.of(author, commenter));

		Post post = createPost(author);
		postRepository.save(post);

		Comment comment = createComment(post, commenter);
		commentRepository.save(comment);

		NotificationCreateRequest request = NotificationCreateRequest.builder()
			.notificationType("COMMENT_CREATION")
			.dataId(comment.getId())
			.build();

		// When
		List<Notification> notifications = notificationTransactionService.create(request);

		// Then
		assertThat(notifications).hasSize(1)
			.extracting("id", "receiver.id", "notificationType", "message", "isRead")
			.containsExactly(tuple(
				notifications.getFirst().getId(),
				author.getId(),
				COMMENT_CREATION,
				commenter.getUsername() + "님이 \"" + post.getTitle() + "\" 게시글에 댓글을 작성했습니다.",
				false)
			);
	}

	@DisplayName("로그인 사용자의 알림을 읽음 처리 할 수 있다")
	@Test
	void markAsRead() {
		// Given
		Notification notification = createCommentCreationNotification(loginMember, false);
		notificationRepository.save(notification);

		// When
		notificationTransactionService.markAsRead(notification.getId());

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
		assertThatThrownBy(() -> notificationTransactionService.markAsRead(notification.getId()))
			.isInstanceOf(CustomException.class)
			.satisfies(ex -> assertThat(((CustomException)ex).getErrorCode())
				.isSameAs(ErrorCode.NOTIFICATION_NO_PERMISSION))
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
		notificationTransactionService.markAllAsRead();

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
		notificationTransactionService.delete(notification.getId());

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
		assertThatThrownBy(() -> notificationTransactionService.delete(notification.getId()))
			.isInstanceOf(CustomException.class)
			.satisfies(ex -> assertThat(((CustomException)ex).getErrorCode())
				.isSameAs(ErrorCode.NOTIFICATION_NO_PERMISSION))
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
		notificationTransactionService.deleteAll(request);

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
		assertThatThrownBy(() -> notificationTransactionService.deleteAll(request))
			.isInstanceOf(CustomException.class)
			.satisfies(ex -> assertThat(((CustomException)ex).getErrorCode())
				.isSameAs(ErrorCode.NOTIFICATION_NO_PERMISSION))
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
			.message("댓글 생성 알림 생성")
			.isRead(isRead)
			.build();
	}

	private Notification createPostCreationNotification(Member receiver, boolean isRead) {
		return Notification.builder()
			.id(UUID.randomUUID())
			.receiver(receiver)
			.notificationType(POST_CREATION)
			.message("게시글 생성 알림 생성")
			.isRead(isRead)
			.build();
	}
}