package api.store.diglog.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import api.store.diglog.model.entity.Member;
import api.store.diglog.model.entity.Subscription;

public interface SubscriptionRepository extends JpaRepository<Subscription, UUID> {

	Optional<Subscription> findByAuthorAndSubscriber(Member author, Member subscriber);

	long countBySubscriber(Member subscriber);
}
