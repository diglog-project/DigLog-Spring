package api.store.diglog.model.entity.notification;

import static api.store.diglog.model.entity.notification.NotificationType.*;

import java.time.LocalDateTime;
import java.util.UUID;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import api.store.diglog.common.exception.CustomException;
import api.store.diglog.common.exception.ErrorCode;
import api.store.diglog.model.entity.Member;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PostLoad;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(
	indexes = {
		@Index(name = "idx_notification_created_at", columnList = "created_at")
	}
)
@EntityListeners(AuditingEntityListener.class)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class Notification {

	@Builder
	public Notification(UUID id, Member receiver, NotificationType notificationType, String message, boolean isRead) {

		validateNotificationType(notificationType);

		this.id = id;
		this.receiver = receiver;
		this.notificationType = notificationType;
		this.message = message;
		this.isRead = isRead;
	}

	@Id
	private UUID id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "receiver_id", nullable = false)
	private Member receiver;

	@Column(nullable = false)
	@Enumerated(EnumType.STRING)
	private NotificationType notificationType;

	@Column(nullable = false)
	private String message;

	@Column(nullable = false)
	private boolean isRead;

	@CreatedDate
	private LocalDateTime createdAt;

	public void markAsRead() {
		this.isRead = true;
	}

	@PostLoad
	private void validateAfterLoad() {
		validateNotificationType(this.notificationType);
	}

	private void validateNotificationType(NotificationType notificationType) {
		if (notificationType == INVALID) {
			throw new CustomException(ErrorCode.NOTIFICATION_INVALID_NOTIFICATION_TYPE);
		}
	}
}
