package api.store.diglog.model.entity;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.hibernate.annotations.ColumnDefault;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@EntityListeners(AuditingEntityListener.class)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Post {

	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
	private UUID id;

	@ManyToOne(fetch = FetchType.LAZY)
	private Member member;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "folder_id")
	private Folder folder;

	@ManyToMany(fetch = FetchType.LAZY)
	@JoinTable(
		name = "post_tag",
		joinColumns = @JoinColumn(name = "post_id"),
		inverseJoinColumns = @JoinColumn(name = "tag_id"))
	private List<Tag> tags;

	@Column(nullable = false)
	private String title;

	@Column(nullable = false, columnDefinition = "TEXT")
	private String content;

	@Column(nullable = false)
	@ColumnDefault("1")
	private long viewCount;

	@Column(nullable = false, columnDefinition = "boolean default false")
	private boolean isDeleted;

	@CreatedDate
	private LocalDateTime createdAt;

	@LastModifiedDate
	private LocalDateTime updatedAt;

	@Builder
	public Post(UUID id, Member member, Folder folder, List<Tag> tags, String title, String content, boolean isDeleted,
		LocalDateTime createdAt, LocalDateTime updatedAt) {
		this.id = id;
		this.member = member;
		this.folder = folder;
		this.tags = tags;
		this.title = title;
		this.content = content;
		this.isDeleted = isDeleted;
		this.createdAt = createdAt;
		this.updatedAt = updatedAt;
	}

	public void updateFolder(Folder folder) {
		this.folder = folder;
	}

	public void updateViewCount(long viewCount) {

		if (this.viewCount > viewCount) {
			throw new IllegalArgumentException("조회수 에러");
		}

		this.viewCount = viewCount;
	}
}
