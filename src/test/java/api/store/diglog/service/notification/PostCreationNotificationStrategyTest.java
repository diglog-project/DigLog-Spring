package api.store.diglog.service.notification;

import static api.store.diglog.model.entity.notification.NotificationType.*;
import static org.assertj.core.api.Assertions.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.transaction.annotation.Transactional;

import api.store.diglog.common.exception.CustomException;
import api.store.diglog.model.constant.Platform;
import api.store.diglog.model.constant.Role;
import api.store.diglog.model.entity.Member;
import api.store.diglog.model.entity.Post;
import api.store.diglog.model.entity.Subscription;
import api.store.diglog.model.entity.notification.NotificationType;
import api.store.diglog.supporter.IntegrationTestSupport;

@Transactional
class PostCreationNotificationStrategyTest extends IntegrationTestSupport {

	@DisplayName("게시글 알림 전략 객체의 알림 타입을 조회할 수 있다")
	@Test
	void getType() {
		// When
		NotificationType notificationType = postCreationNotificationStrategy.getType();

		// Then
		assertThat(notificationType).isEqualTo(POST_CREATION);
	}

	@DisplayName("게시글 알림 수신자 목록을 조회할 수 있다")
	@Test
	void resolveReceivers() {
		// Given
		Member author = createMember("author");
		Member receiver = createMember("receiver");
		memberRepository.saveAll(List.of(author, receiver));

		Subscription subscription = createSubscription(author, receiver, true);
		subscriptionRepository.save(subscription);

		Post post = createPost(author);
		postRepository.save(post);

		// When
		List<Member> receivers = postCreationNotificationStrategy.resolveReceivers(post.getId());

		// Then
		assertThat(receivers).hasSize(1)
			.containsExactly(receiver);
	}

	@DisplayName("구독 알림 여부를 활성화한 수신자만 조회된다")
	@Test
	void resolveReceivers_ActiveNotificationEnabled() {
		// Given
		Member author = createMember("author");
		Member receiver01 = createMember("receiver01");
		Member receiver02 = createMember("receiver02");
		Member receiver03 = createMember("receiver03");
		memberRepository.saveAll(List.of(author, receiver01, receiver02, receiver03));

		Subscription subscription01 = createSubscription(author, receiver01, true);
		Subscription subscription02 = createSubscription(author, receiver02, false);
		Subscription subscription03 = createSubscription(author, receiver03, true);
		subscriptionRepository.saveAll(List.of(subscription01, subscription02, subscription03));

		Post post = createPost(author);
		postRepository.save(post);

		// When
		List<Member> receivers = postCreationNotificationStrategy.resolveReceivers(post.getId());

		// Then
		assertThat(receivers).hasSize(2)
			.containsExactlyInAnyOrder(receiver01, receiver03);
	}

	@DisplayName("게시글이 없는 경우, 알림 수신자를 조회할 수 없다")
	@Test
	void resolveReceivers_NotExistPost() {
		// When, Then
		assertThatThrownBy(() -> postCreationNotificationStrategy.resolveReceivers(UUID.randomUUID()))
			.isInstanceOf(CustomException.class)
			.hasMessage("해당 게시글이 없습니다.");
	}

	@DisplayName("알림 메세지를 생성할 수 있다")
	@Test
	void generateMessage() {
		// Given
		Member author = createMember("author");
		Member receiver01 = createMember("receiver01");
		Member receiver02 = createMember("receiver02");
		Member receiver03 = createMember("receiver03");
		memberRepository.saveAll(List.of(author, receiver01, receiver02, receiver03));

		Subscription subscription01 = createSubscription(author, receiver01, true);
		Subscription subscription02 = createSubscription(author, receiver02, false);
		Subscription subscription03 = createSubscription(author, receiver03, true);
		subscriptionRepository.saveAll(List.of(subscription01, subscription02, subscription03));

		Post post = createPost(author);
		postRepository.save(post);

		// When
		String message = postCreationNotificationStrategy.generateMessage(post.getId());

		// then
		assertThat(message).isEqualTo(
			String.format("%s님이 \"%s\" 게시글을 작성했습니다.", author.getUsername(), post.getTitle())
		);

	}

	@DisplayName("게시글이 없는 경우, 알림 메세지를 생성할 수 없다")
	@Test
	void generateMessage_NotExistPost() {
		// When, Then
		assertThatThrownBy(() -> postCreationNotificationStrategy.generateMessage(UUID.randomUUID()))
			.isInstanceOf(CustomException.class)
			.hasMessage("해당 게시글이 없습니다.");
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

}