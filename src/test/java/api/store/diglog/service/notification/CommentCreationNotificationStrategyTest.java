package api.store.diglog.service.notification;

import static api.store.diglog.model.entity.notification.NotificationType.*;
import static org.assertj.core.api.Assertions.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import api.store.diglog.common.exception.CustomException;
import api.store.diglog.model.constant.Platform;
import api.store.diglog.model.constant.Role;
import api.store.diglog.model.entity.Comment;
import api.store.diglog.model.entity.Member;
import api.store.diglog.model.entity.Post;
import api.store.diglog.model.entity.notification.NotificationType;
import api.store.diglog.repository.CommentRepository;
import api.store.diglog.repository.MemberRepository;
import api.store.diglog.repository.PostRepository;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class CommentCreationNotificationStrategyTest {

	@Autowired
	private MemberRepository memberRepository;

	@Autowired
	private PostRepository postRepository;

	@Autowired
	private CommentRepository commentRepository;

	@Autowired
	private CommentCreationNotificationStrategy commentCreationNotificationStrategy;

	@DisplayName("댓글 알림 전략 객체의 알림 타입을 조회할 수 있다")
	@Test
	void getType() {
		// When
		NotificationType notificationType = commentCreationNotificationStrategy.getType();

		// Then
		assertThat(notificationType).isEqualTo(COMMENT_CREATION);
	}

	@DisplayName("댓글 알림 수신자를 조회할 수 있다")
	@Test
	void resolveReceivers() {
		// Given
		Member author = createMember("author");
		Member commenter = createMember("commenter");
		memberRepository.saveAll(List.of(author, commenter));

		Post post = createPost(author);
		postRepository.save(post);

		Comment comment = createComment(post, commenter);
		commentRepository.save(comment);

		// When
		List<Member> receivers = commentCreationNotificationStrategy.resolveReceivers(comment.getId());

		// Then
		assertThat(receivers).hasSize(1)
			.containsExactly(author);
	}

	@DisplayName("댓글이 없는 경우, 알림 수신자를 조회할 수 없다")
	@Test
	void resolveReceivers_NotExistComment() {
		// When, Then
		assertThatThrownBy(() -> commentCreationNotificationStrategy.resolveReceivers(UUID.randomUUID()))
			.isInstanceOf(CustomException.class)
			.hasMessage("해당 댓글을 찾을 수 없습니다.");
	}

	@DisplayName("게시글 작성자는 댓글 알림 수신자가 될 수 없다")
	@Test
	void resolveReceivers_SelfComment() {
		// Given
		Member author = createMember("author");
		memberRepository.save(author);

		Post post = createPost(author);
		postRepository.save(post);

		Comment comment = createComment(post, author);
		commentRepository.save(comment);

		// When
		assertThatThrownBy(() -> commentCreationNotificationStrategy.resolveReceivers(comment.getId()))
			.isInstanceOf(CustomException.class)
			.hasMessage("게시글과 댓글의 작성자가 일치하는 경우엔 알림이 생성되지 않습니다.");
	}

	@DisplayName("댓글 알림 메세지를 생성할 수 있다")
	@Test
	void generateMessage() {
		// Given
		Member author = createMember("author");
		Member commenter = createMember("commenter");
		memberRepository.saveAll(List.of(author, commenter));

		Post post = createPost(author);
		postRepository.save(post);

		Comment comment = createComment(post, commenter);
		commentRepository.save(comment);

		// When
		String message = commentCreationNotificationStrategy.generateMessage(comment.getId());

		// Then
		assertThat(message).isEqualTo(
			String.format("%s님이 \"%s\" 게시글에 댓글을 작성했습니다.", commenter.getUsername(), post.getTitle())
		);
	}

	@DisplayName("댓글이 없는 경우, 알림 메세지를 생성할 수 없다")
	@Test
	void generateMessage_NotExistComment() {
		// When, Then
		assertThatThrownBy(() -> commentCreationNotificationStrategy.generateMessage(UUID.randomUUID()))
			.isInstanceOf(CustomException.class)
			.hasMessage("해당 댓글을 찾을 수 없습니다.");
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

	private Comment createComment(Post post, Member commenter) {
		return Comment.builder()
			.post(post)
			.member(commenter)
			.content("comment content")
			.build();
	}
}