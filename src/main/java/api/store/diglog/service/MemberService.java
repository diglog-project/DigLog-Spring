package api.store.diglog.service;

import api.store.diglog.common.auth.JWTUtil;
import api.store.diglog.common.exception.CustomException;
import api.store.diglog.common.util.SecurityUtil;
import api.store.diglog.model.constant.Role;
import api.store.diglog.model.dto.login.LoginRequest;
import api.store.diglog.model.dto.login.LogoutRequest;
import api.store.diglog.model.dto.member.MemberInfoResponse;
import api.store.diglog.model.dto.member.MemberProfileResponse;
import api.store.diglog.model.dto.member.MemberUsernameRequest;
import api.store.diglog.model.entity.Member;
import api.store.diglog.model.vo.login.LoginTokenVO;
import api.store.diglog.model.vo.login.LogoutTokenVO;
import api.store.diglog.repository.MemberRepository;
import jakarta.servlet.http.Cookie;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.stream.Collectors;

import static api.store.diglog.common.exception.ErrorCode.LOGIN_INPUT_CREDENTIALS_MISMATCH;
import static api.store.diglog.common.exception.ErrorCode.MEMBER_EMAIL_NOT_FOUND;

@Service
@RequiredArgsConstructor
public class MemberService {

    private final MemberRepository memberRepository;
    private final RefreshService refreshService;
    private final PasswordEncoder passwordEncoder;
    private final JWTUtil jwtUtil;

    // 현재 api 요청을 보낸 Member
    public Member getCurrentMember() {
        String email = SecurityUtil.getAuthenticationMemberInfo().getEmail();
        return memberRepository.findByEmail(email)
                .orElseThrow(() -> new CustomException(MEMBER_EMAIL_NOT_FOUND));
    }

    public LoginTokenVO login(LoginRequest loginRequest) {
        Member member = memberRepository.findByEmail(loginRequest.getEmail())
                .orElseThrow(() -> new CustomException(LOGIN_INPUT_CREDENTIALS_MISMATCH));

        if (!passwordEncoder.matches(loginRequest.getPassword(), member.getPassword())) {
            throw new CustomException(LOGIN_INPUT_CREDENTIALS_MISMATCH);
        }

        String accessToken = jwtUtil.generateAccessToken(member);

        Cookie refreshTokenCookie = jwtUtil.generateRefreshCookie(member);
        refreshService.save(member.getEmail(), refreshTokenCookie.getValue());

        MemberInfoResponse memberInfoResponse = MemberInfoResponse.builder()
                .status(200)
                .email(member.getEmail())
                .username(member.getUsername())
                .roles(member.getRoles().stream().map(Role::getRole).collect(Collectors.toSet()))
                .build();

        return LoginTokenVO.builder()
                .accessToken(accessToken)
                .refreshTokenCookie(refreshTokenCookie)
                .memberInfoResponse(memberInfoResponse)
                .build();
    }

    public LogoutTokenVO logout(LogoutRequest logoutRequest) {

        Cookie logoutCookie = jwtUtil.generateLogoutCookie();
        refreshService.delete(logoutRequest.getEmail());

        return LogoutTokenVO.builder()
                .logoutCookie(logoutCookie)
                .build();
    }

    public void updateUsername(MemberUsernameRequest memberUsernameRequest) {
        String email = SecurityUtil.getAuthenticationMemberInfo().getEmail();
        memberRepository.updateUsername(memberUsernameRequest.getUsername(), email);
    }

    public MemberProfileResponse getProfile() {
        String email = SecurityUtil.getAuthenticationMemberInfo().getEmail();
        Member member = memberRepository.findByEmail(email)
                .orElseThrow(() -> new CustomException(LOGIN_INPUT_CREDENTIALS_MISMATCH));

        return MemberProfileResponse.builder()
                .email(email)
                .username(member.getUsername())
                .build();
    }
}
