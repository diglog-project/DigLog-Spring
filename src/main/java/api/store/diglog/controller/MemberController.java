package api.store.diglog.controller;

import api.store.diglog.model.dto.image.ImageRequest;
import api.store.diglog.model.dto.image.ImageUrlResponse;
import api.store.diglog.model.dto.member.MemberProfileInfoResponse;
import api.store.diglog.model.dto.member.MemberProfileResponse;
import api.store.diglog.model.dto.member.MemberUsernameRequest;
import api.store.diglog.service.MemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/member")
@RequiredArgsConstructor
public class MemberController {

    private final MemberService memberService;

    @PostMapping("/username")
    public ResponseEntity<?> updateUsername(@RequestBody MemberUsernameRequest memberUsernameRequest) {
        memberService.updateUsername(memberUsernameRequest);

        return ResponseEntity.ok().build();
    }

    @GetMapping("/profile")
    public ResponseEntity<?> getProfile() {
        MemberProfileResponse memberInfoResponse = memberService.getProfile();

        return ResponseEntity.ok().body(memberInfoResponse);
    }

    @GetMapping("/profile/{username}")
    public ResponseEntity<MemberProfileInfoResponse> getProfileByUsername(@PathVariable String username) {
        MemberProfileInfoResponse memberProfileInfoResponse = memberService.getProfileByUsername(username);

        return ResponseEntity.ok().body(memberProfileInfoResponse);
    }

    @PostMapping(
            value = "/image",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ImageUrlResponse> uploadAndSaveImage(@RequestBody ImageRequest imageRequest) {
        ImageUrlResponse imageUrlResponse = memberService.updateProfileImage(imageRequest);

        return ResponseEntity.ok().body(imageUrlResponse);
    }
}
