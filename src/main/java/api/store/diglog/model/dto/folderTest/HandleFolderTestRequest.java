package api.store.diglog.model.dto.folderTest;

import api.store.diglog.model.entity.FolderTest;
import api.store.diglog.model.entity.Member;
import lombok.*;
import software.amazon.awssdk.annotations.NotNull;

import java.util.UUID;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PROTECTED)
public class HandleFolderTestRequest {

    @NotNull
    private UUID id;
    @NotNull
    private String title;
    private UUID parentId;

    public FolderTest toFolderTest(Member member, int orderIndex) {
        return FolderTest.builder()
                .id(id)
                .member(member)
                .title(this.title)
                .orderIndex(orderIndex)
                .parentId(this.parentId)
                .build();
    }
}
