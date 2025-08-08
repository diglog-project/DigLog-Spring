package api.store.diglog.repository;

import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import api.store.diglog.model.entity.Member;
import api.store.diglog.model.entity.notification.Notification;

public interface NotificationRepository extends JpaRepository<Notification, UUID> {

	Page<Notification> findAllByReceiver(Member receiver, Pageable pageable);

}
