package api.store.diglog.service;

import api.store.diglog.common.exception.CustomException;
import api.store.diglog.common.util.SecurityUtil;
import api.store.diglog.model.dto.image.ImageRequest;
import api.store.diglog.model.dto.image.ImageUrlResponse;
import api.store.diglog.model.dto.login.LoginRequest;
import api.store.diglog.model.dto.member.MemberProfileInfoResponse;
import api.store.diglog.model.dto.member.MemberProfileResponse;
import api.store.diglog.model.dto.member.MemberUsernameRequest;
import api.store.diglog.model.entity.Member;
import api.store.diglog.model.vo.image.ImageSaveVO;
import api.store.diglog.repository.MemberRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.UUID;

import static api.store.diglog.common.exception.ErrorCode.*;

@Service
@RequiredArgsConstructor
public class MemberService {

    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;
    private final ImageService imageService;

    // 현재 api 요청을 보낸 Member
    public Member getCurrentMember() {
        String email = SecurityUtil.getAuthenticationMemberInfo().getEmail();
        return memberRepository.findByEmail(email)
                .orElseThrow(() -> new CustomException(MEMBER_EMAIL_NOT_FOUND));
    }

    public Member login(LoginRequest loginRequest) {
        Member member = memberRepository.findByEmail(loginRequest.getEmail())
                .orElseThrow(() -> new CustomException(LOGIN_FAILED));

        if (!passwordEncoder.matches(loginRequest.getPassword(), member.getPassword())) {
            throw new CustomException(LOGIN_FAILED);
        }

        return member;
    }

    public void updateUsername(MemberUsernameRequest memberUsernameRequest) {
        String email = SecurityUtil.getAuthenticationMemberInfo().getEmail();
        memberRepository.updateUsername(memberUsernameRequest.getUsername(), email);
    }

    public MemberProfileResponse getProfile() {
        Member member = getCurrentMember();

        return MemberProfileResponse.builder()
                .email(member.getEmail())
                .username(member.getUsername())
                .profileUrl(imageService.getUrlByRefId(member.getId()).getUrl())
                .build();
    }

    public MemberProfileInfoResponse getProfileByUsername(String username) {
        Member member = memberRepository.findByUsername(username)
                .orElseThrow(() -> new CustomException(MEMBER_USERNAME_NOT_FOUND));

        return MemberProfileInfoResponse.builder()
                .username(member.getUsername())
                .profileUrl(imageService.getUrlByRefId(member.getId()).getUrl())
                .build();
    }

    @Transactional
    public ImageUrlResponse updateProfileImage(ImageRequest imageRequest) {
        UUID refId = getCurrentMember().getId();
        ImageSaveVO imageSaveVO = ImageSaveVO.builder()
                .refId(refId)
                .file(imageRequest.getFile())
                .build();

        return imageService.uploadAndSaveImage(imageSaveVO);
    }
}
