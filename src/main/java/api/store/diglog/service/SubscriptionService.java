package api.store.diglog.service;

import static api.store.diglog.common.exception.ErrorCode.*;

import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import api.store.diglog.common.exception.CustomException;
import api.store.diglog.model.dto.subscribe.SubscriberResponse;
import api.store.diglog.model.dto.subscribe.SubscriptionCreateRequest;
import api.store.diglog.model.dto.subscribe.SubscriptionCreateResponse;
import api.store.diglog.model.dto.subscribe.SubscriptionNotificationActivationRequest;
import api.store.diglog.model.dto.subscribe.SubscriptionResponse;
import api.store.diglog.model.entity.Member;
import api.store.diglog.model.entity.Subscription;
import api.store.diglog.repository.SubscriptionRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SubscriptionService {

	private static final long SUBSCRIPTION_COUNT_LIMIT = 1000;

	private final SubscriptionRepository subscriptionRepository;
	private final MemberService memberService;

	public List<SubscriptionResponse> getUserSubscriptions(UUID userId, int page, int size) {
		Member user = memberService.findActiveMemberById(userId);
		PageRequest pageRequest = PageRequest.of(page, size, Sort.by("createdAt").descending());
		Page<Subscription> findSubscription =
			subscriptionRepository.findAllBySubscriberAndAuthorIsDeletedFalse(user, pageRequest);
		return findSubscription.map(SubscriptionResponse::from)
			.toList();
	}

	public List<SubscriberResponse> getAuthorSubscribers(UUID authorId, int page, int size) {
		Member author = memberService.findActiveMemberById(authorId);
		PageRequest pageRequest = PageRequest.of(page, size, Sort.by("createdAt").descending());
		Page<Subscription> findSubscription =
			subscriptionRepository.findAllByAuthorAndSubscriberIsDeletedFalse(author, pageRequest);
		return findSubscription.map(SubscriberResponse::from)
			.toList();
	}

	@Transactional
	public SubscriptionCreateResponse createSubscription(SubscriptionCreateRequest subscriptionCreateRequest) {
		Member author = memberService.findMemberById(subscriptionCreateRequest.getAuthorId());
		Member subscriber = memberService.getCurrentMember();

		validateActiveAuthor(author);
		validateSelfSubscription(author, subscriber);
		validateAlreadySubscribed(author, subscriber);
		validateSubscriptionLimits(subscriber);

		Subscription subscription = Subscription.builder()
			.id(UUID.randomUUID())
			.author(author)
			.subscriber(subscriber)
			.notificationEnabled(subscriptionCreateRequest.getNotificationEnabled())
			.build();
		Subscription savedSubscription = subscriptionRepository.save(subscription);

		return SubscriptionCreateResponse.builder()
			.authorId(author.getId())
			.authorNickname(author.getUsername())
			.subscriberId(subscriber.getId())
			.subscriberNickname(subscriber.getUsername())
			.notificationEnabled(savedSubscription.isNotificationEnabled())
			.createdAt(savedSubscription.getCreatedAt())
			.build();
	}

	@Transactional
	public void updateNotificationSetting(
		UUID subscriptionId,
		SubscriptionNotificationActivationRequest request
	) {
		Member currentMember = memberService.getCurrentMember();
		Subscription subscription = subscriptionRepository.findByIdFetchSubscriber(subscriptionId)
			.orElseThrow(() -> new CustomException(SUBSCRIPTION_NOT_FOUND));

		validateCurrentMemberIsSubscriber(currentMember, subscription.getSubscriber());

		if (request.getNotificationEnabled()) {
			subscription.enableNotification();
		} else {
			subscription.disableNotification();
		}
	}

	private void validateCurrentMemberIsSubscriber(Member currentMember, Member subscriber) {
		if (isDifferentMember(currentMember, subscriber)) {
			throw new CustomException(SUBSCRIPTION_MISMATCH_CURRENT_MEMBER_SUBSCRIBER);
		}
	}

	private boolean isDifferentMember(Member currentMember, Member subscriber) {
		return !subscriber.equals(currentMember);
	}

	private void validateActiveAuthor(Member author) {
		if (author.isDeleted()) {
			throw new CustomException(SUBSCRIPTION_INACTIVE_AUTHOR);
		}
	}

	private void validateSelfSubscription(Member author, Member subscriber) {
		if (author.getId().equals(subscriber.getId())) {
			throw new CustomException(SUBSCRIPTION_SELF_SUBSCRIPTION);
		}
	}

	private void validateAlreadySubscribed(Member author, Member subscriber) {
		subscriptionRepository.findByAuthorAndSubscriber(author, subscriber)
			.ifPresent(subscription -> {
				throw new CustomException(SUBSCRIPTION_ALREADY_SUBSCRIPTION);
			});
	}

	private void validateSubscriptionLimits(Member subscriber) {
		long subscriptionCount = subscriptionRepository.countBySubscriber(subscriber);
		if (subscriptionCount >= SUBSCRIPTION_COUNT_LIMIT) {
			throw new CustomException(
				SUBSCRIPTION_EXCEED_SUBSCRIPTION_COUNT,
				String.format(
					SUBSCRIPTION_EXCEED_SUBSCRIPTION_COUNT.getMessage(),
					SUBSCRIPTION_COUNT_LIMIT
				));
		}
	}

}
