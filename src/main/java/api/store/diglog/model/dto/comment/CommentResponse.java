package api.store.diglog.model.dto.comment;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Builder
public class CommentResponse {

    private final UUID id;
    private final CommentMember member;
    private final String content;
    private final boolean isDeleted;
    private final String taggedUsername;
    private final LocalDateTime createdAt;
    private final int replyCount;
}
