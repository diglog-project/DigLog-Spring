package api.store.diglog.model.entity;

import java.time.LocalDateTime;
import java.util.UUID;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(
	name = "subscription",
	uniqueConstraints = {
		@UniqueConstraint(
			name = "uk_subscription_author_subscriber",
			columnNames = {"author_id", "subscriber_id"}
		)
	},
	indexes = {
		@Index(name = "idx_subscription_author", columnList = "author_id"),
		@Index(name = "idx_subscription_subscriber", columnList = "subscriber_id"),
	}
)
@EntityListeners(AuditingEntityListener.class)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@Getter
public class Subscription {

	@Id
	private UUID id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "author_id", nullable = false)
	private Member author;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "subscriber_id", nullable = false)
	private Member subscriber;

	@Column(nullable = false)
	@Builder.Default
	private boolean notificationEnabled = true;

	@CreatedDate
	@Column(nullable = false, updatable = false)
	private LocalDateTime createdAt;

	public void enableNotification() {
		this.notificationEnabled = true;
	}

	public void disableNotification() {
		this.notificationEnabled = false;
	}

}
