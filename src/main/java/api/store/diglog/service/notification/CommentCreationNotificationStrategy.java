package api.store.diglog.service.notification;

import static api.store.diglog.common.exception.ErrorCode.*;
import static api.store.diglog.model.entity.notification.NotificationType.*;
import static java.util.Collections.*;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Component;

import api.store.diglog.common.exception.CustomException;
import api.store.diglog.model.entity.Comment;
import api.store.diglog.model.entity.Member;
import api.store.diglog.model.entity.notification.NotificationType;
import api.store.diglog.repository.CommentRepository;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class CommentCreationNotificationStrategy implements NotificationStrategy {

	private static final String NOTIFICATION_MESSAGE_FORMAT = "%s님이 \"%s\" 게시글에 댓글을 작성했습니다.";

	private final CommentRepository commentRepository;

	@Override
	public NotificationType getType() {
		return COMMENT_CREATION;
	}

	@Override
	public List<Member> resolveReceivers(UUID dataId) {
		Comment comment = searchCommentBy(dataId);

		Member author = comment.getPost().getMember();
		validateSelfCommentNotification(author, comment.getMember());

		return singletonList(author);
	}

	@Override
	public String generateMessage(UUID dataId) {
		Comment comment = searchCommentBy(dataId);

		return String.format(
			NOTIFICATION_MESSAGE_FORMAT,
			comment.getMember().getUsername(),
			comment.getPost().getTitle()
		);
	}

	private Comment searchCommentBy(UUID dataId) {
		return commentRepository.findByIdAndIsDeletedFalse(dataId)
			.orElseThrow(() -> new CustomException(COMMENT_NOT_FOUND));
	}

	private void validateSelfCommentNotification(Member author, Member commenter) {
		if (commenter.equals(author)) {
			throw new CustomException(NOTIFICATION_SELF_COMMENT);
		}
	}

}
