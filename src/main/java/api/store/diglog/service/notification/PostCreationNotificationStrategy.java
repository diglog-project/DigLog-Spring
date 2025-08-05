package api.store.diglog.service.notification;

import static api.store.diglog.model.entity.notification.NotificationType.*;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Component;

import api.store.diglog.common.exception.CustomException;
import api.store.diglog.common.exception.ErrorCode;
import api.store.diglog.model.entity.Member;
import api.store.diglog.model.entity.Post;
import api.store.diglog.model.entity.Subscription;
import api.store.diglog.model.entity.notification.NotificationType;
import api.store.diglog.repository.PostRepository;
import api.store.diglog.repository.SubscriptionRepository;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class PostCreationNotificationStrategy implements NotificationStrategy {

	private static final String NOTIFICATION_MESSAGE_FORMAT = "%s님이 \"%s\" 새 게시글을 작성했습니다.";

	private final PostRepository postRepository;
	private final SubscriptionRepository subscriptionRepository;

	@Override
	public NotificationType getType() {
		return POST_CREATION;
	}

	@Override
	public List<Member> resolveReceivers(UUID dataId) {
		Post post = searchPostBy(dataId);

		List<Subscription> subscriptions =
			subscriptionRepository.findAllByAuthorAndSubscriberIsDeletedFalse(post.getMember());

		return subscriptions.stream()
			.filter(Subscription::isNotificationEnabled)
			.map(Subscription::getSubscriber)
			.toList();
	}

	@Override
	public String generateMessage(UUID dataId) {
		Post post = searchPostBy(dataId);
		return String.format(
			NOTIFICATION_MESSAGE_FORMAT,
			post.getMember().getUsername(),
			post.getTitle()
		);
	}

	private Post searchPostBy(UUID dataId) {
		return postRepository.findByIdAndIsDeletedFalse(dataId)
			.orElseThrow(() -> new CustomException(ErrorCode.POST_NOT_FOUND));
	}
}
