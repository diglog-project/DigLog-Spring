package api.store.diglog.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import api.store.diglog.model.entity.Member;
import api.store.diglog.model.entity.Subscription;

public interface SubscriptionRepository extends JpaRepository<Subscription, UUID> {

	Optional<Subscription> findByAuthorAndSubscriber(Member author, Member subscriber);

	long countBySubscriber(Member subscriber);

	@EntityGraph(attributePaths = "author")
	Page<Subscription> findAllBySubscriberAndAuthorIsDeletedFalse(Member subscriber, Pageable pageable);

	@EntityGraph(attributePaths = "subscriber")
	Page<Subscription> findAllByAuthorAndSubscriberIsDeletedFalse(Member author, Pageable pageable);

}
