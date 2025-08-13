package api.store.diglog.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import api.store.diglog.model.entity.Member;
import api.store.diglog.model.entity.Subscription;

public interface SubscriptionRepository extends JpaRepository<Subscription, UUID> {

	@Query("select s from Subscription s join fetch s.subscriber where s.id = :id")
	Optional<Subscription> findByIdFetchSubscriber(@Param("id") UUID id);

	Optional<Subscription> findByAuthorAndSubscriber(Member author, Member subscriber);

	long countBySubscriber(Member subscriber);

	@EntityGraph(attributePaths = "author")
	Page<Subscription> findAllBySubscriberAndAuthorIsDeletedFalse(Member subscriber, Pageable pageable);

	@EntityGraph(attributePaths = "subscriber")
	Page<Subscription> findAllByAuthorAndSubscriberIsDeletedFalse(Member author, Pageable pageable);

	@EntityGraph(attributePaths = "subscriber")
	List<Subscription> findAllByAuthorAndSubscriberIsDeletedFalse(Member author);

	boolean existsBySubscriberAndAuthor(Member subscriber, Member author);

}
