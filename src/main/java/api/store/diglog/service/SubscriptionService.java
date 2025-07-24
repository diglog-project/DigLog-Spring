package api.store.diglog.service;

import static api.store.diglog.common.exception.ErrorCode.*;

import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import api.store.diglog.common.exception.CustomException;
import api.store.diglog.model.dto.subscribe.SubscribeCreateRequest;
import api.store.diglog.model.dto.subscribe.SubscribeCreateResponse;
import api.store.diglog.model.entity.Member;
import api.store.diglog.model.entity.Subscription;
import api.store.diglog.repository.SubscriptionRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class SubscriptionService {

	private static final long SUBSCRIPTION_COUNT_LIMIT = 1000;

	private final SubscriptionRepository subscriptionRepository;
	private final MemberService memberService;

	@Transactional
	public SubscribeCreateResponse createSubscription(SubscribeCreateRequest subscribeCreateRequest) {
		Member author = memberService.findMemberById(subscribeCreateRequest.getAuthorId());
		Member subscriber = memberService.getCurrentMember();

		validateActiveAuthor(author);
		validateSelfSubscription(author, subscriber);
		validateAlreadySubscribed(author, subscriber);
		validateSubscriptionLimits(subscriber);

		Subscription subscription = Subscription.builder()
			.id(UUID.randomUUID())
			.author(author)
			.subscriber(subscriber)
			.notificationEnabled(subscribeCreateRequest.getNotificationEnabled())
			.build();
		Subscription savedSubscription = subscriptionRepository.save(subscription);

		return SubscribeCreateResponse.builder()
			.authorId(author.getId())
			.authorNickname(author.getUsername())
			.subscriberId(subscriber.getId())
			.subscriberNickname(subscriber.getUsername())
			.notificationEnabled(savedSubscription.isNotificationEnabled())
			.createdAt(savedSubscription.getCreatedAt())
			.build();
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
