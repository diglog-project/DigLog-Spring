package api.store.diglog.controller;

import api.store.diglog.model.dto.member.MemberProfileResponse;
import api.store.diglog.model.dto.member.MemberUsernameRequest;
import api.store.diglog.service.MemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/member")
@RequiredArgsConstructor
public class MemberController {

    private final MemberService memberService;

    @PostMapping("/username")
    public ResponseEntity<Void> updateUsername(@RequestBody MemberUsernameRequest memberUsernameRequest) {
        memberService.updateUsername(memberUsernameRequest);

        return ResponseEntity.ok().build();
    }

    @GetMapping("/profile")
    public ResponseEntity<MemberProfileResponse> getProfile() {
        MemberProfileResponse memberInfoResponse = memberService.getProfile();

        return ResponseEntity.ok().body(memberInfoResponse);
    }
}
