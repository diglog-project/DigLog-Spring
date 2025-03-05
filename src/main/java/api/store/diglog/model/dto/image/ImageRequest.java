package api.store.diglog.model.dto.image;

import lombok.*;
import org.springframework.web.multipart.MultipartFile;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PROTECTED)
public class ImageRequest {

    private MultipartFile file;
}
