package api.store.diglog.model.dto.member;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class MemberProfileResponseDTO {

    private String email;
    private String username;
}
