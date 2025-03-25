package api.store.diglog.model.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.util.UUID;

@Entity
@EntityListeners(AuditingEntityListener.class)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class FolderTest {

    @Id
    UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    private Member member;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false)
    private int orderIndex;

    @Column
    private UUID parentId;

    @Builder
    public FolderTest(UUID id, Member member, String title, int orderIndex, UUID parentId) {
        this.id = id;
        this.member = member;
        this.title = title;
        this.orderIndex = orderIndex;
        this.parentId = parentId;
    }
}
