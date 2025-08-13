package api.store.diglog.service.notification;

import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import api.store.diglog.model.dto.notification.NotificationCreateRequest;
import api.store.diglog.model.dto.notification.NotificationDeleteRequest;
import api.store.diglog.model.dto.notification.NotificationDeleteResponse;
import api.store.diglog.model.dto.notification.NotificationReadResponse;
import api.store.diglog.model.dto.notification.NotificationResponse;
import api.store.diglog.model.entity.Member;
import api.store.diglog.model.entity.notification.Notification;
import api.store.diglog.repository.NotificationRepository;
import api.store.diglog.service.MemberService;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class NotificationService {

	private final NotificationRepository notificationRepository;
	private final MemberService memberService;
	private final NotificationTransactionService notificationTransactionService;
	private final NotificationPublisher notificationPublisher;

	public void createAndPublish(NotificationCreateRequest request) {
		List<Notification> notifications = notificationTransactionService.create(request);
		notificationPublisher.publish(notifications);
	}

	public Page<NotificationResponse> searchBy(int page, int size) {
		Member receiver = memberService.getCurrentMember();
		PageRequest pageRequest = PageRequest.of(page, size, Sort.by("createdAt").descending());
		return notificationRepository.findAllByReceiver(receiver, pageRequest)
			.map(NotificationResponse::from);
	}

	public NotificationReadResponse markAsRead(UUID notificationId) {
		return notificationTransactionService.markAsRead(notificationId);
	}

	public List<NotificationReadResponse> markAllAsRead() {
		return notificationTransactionService.markAllAsRead();
	}

	public void delete(UUID notificationId) {
		notificationTransactionService.delete(notificationId);
	}

	public NotificationDeleteResponse deleteAll(NotificationDeleteRequest request) {
		return notificationTransactionService.deleteAll(request);
	}
}
