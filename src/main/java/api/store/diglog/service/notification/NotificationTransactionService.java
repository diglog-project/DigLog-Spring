package api.store.diglog.service.notification;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import api.store.diglog.model.dto.notification.NotificationCreateRequest;
import api.store.diglog.model.dto.notification.NotificationDeleteRequest;
import api.store.diglog.model.dto.notification.NotificationDeleteResponse;
import api.store.diglog.model.dto.notification.NotificationReadResponse;
import api.store.diglog.model.entity.Member;
import api.store.diglog.model.entity.notification.Notification;
import api.store.diglog.model.entity.notification.NotificationType;
import api.store.diglog.repository.NotificationRepository;
import api.store.diglog.service.MemberService;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class NotificationTransactionService {

	private final NotificationRepository notificationRepository;
	private final NotificationStrategyFactory notificationStrategyFactory;
	private final MemberService memberService;

	@Transactional
	public List<Notification> create(NotificationCreateRequest request) {
		NotificationType notificationType = NotificationType.from(request.getNotificationType());
		NotificationStrategy strategy = notificationStrategyFactory.getStrategy(notificationType);

		UUID dataId = request.getDataId();
		List<Member> receivers = strategy.resolveReceivers(dataId);
		String message = strategy.generateMessage(dataId);

		List<Notification> notifications = receivers.stream()
			.map(receiver -> Notification.builder()
				.id(UUID.randomUUID())
				.receiver(receiver)
				.notificationType(notificationType)
				.message(message)
				.isRead(false)
				.build())
			.toList();

		return notificationRepository.saveAll(notifications);
	}

	@Transactional
	public NotificationReadResponse markAsRead(UUID notificationId) {
		Member currentMember = memberService.getCurrentMember();
		Notification notification = notificationRepository.findById(notificationId)
			.orElseThrow(() -> new IllegalArgumentException("에러"));

		validateReceiverCurrentMemberSame(notification.getReceiver(), currentMember);

		notification.markAsRead();
		return NotificationReadResponse.from(notification);
	}

	@Transactional
	public List<NotificationReadResponse> markAllAsRead() {
		Member currentMember = memberService.getCurrentMember();
		List<Notification> notifications = notificationRepository.findAllByReceiverAndIsReadFalse(currentMember);
		return notifications.stream()
			.map(notification -> {
				validateReceiverCurrentMemberSame(notification.getReceiver(), currentMember);
				notification.markAsRead();
				return NotificationReadResponse.from(notification);
			})
			.toList();
	}

	@Transactional
	public void delete(UUID notificationId) {
		Member currentMember = memberService.getCurrentMember();
		Notification notification = notificationRepository.findById(notificationId)
			.orElseThrow(() -> new IllegalArgumentException("에러 처리"));

		validateReceiverCurrentMemberSame(notification.getReceiver(), currentMember);

		notificationRepository.delete(notification);
	}

	@Transactional
	public NotificationDeleteResponse deleteAll(NotificationDeleteRequest request) {
		Member currentMember = memberService.getCurrentMember();
		List<Notification> notifications = notificationRepository.findAllById(request.getNotificationIds());

		notifications.forEach(notification ->
			validateReceiverCurrentMemberSame(notification.getReceiver(), currentMember));

		notificationRepository.deleteAllInBatch(notifications);
		return NotificationDeleteResponse.of(request.getNotificationIds().size(), notifications.size());
	}

	private void validateReceiverCurrentMemberSame(Member receiver, Member currentMember) {
		if (receiver.isDifferent(currentMember)) {
			throw new IllegalArgumentException("에러 처리");
		}
	}

}
