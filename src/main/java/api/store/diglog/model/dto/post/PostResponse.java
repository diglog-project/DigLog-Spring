package api.store.diglog.model.dto.post;

import api.store.diglog.model.dto.tag.TagResponse;
import api.store.diglog.model.entity.Post;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
public class PostResponse {

    private UUID id;
    private String title;
    private String content;
    private String username;
    private List<TagResponse> tags;
    private LocalDateTime createdAt;

    public PostResponse(Post post) {
        this.id = post.getId();
        this.title = post.getTitle();
        this.content = post.getContent();
        this.username = post.getMember().getUsername();
        this.tags = post.getTags().stream()
                .map(TagResponse::new)
                .toList();
        this.createdAt = post.getCreatedAt();
    }
}
