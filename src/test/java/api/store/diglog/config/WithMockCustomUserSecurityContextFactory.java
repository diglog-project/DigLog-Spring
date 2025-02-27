package api.store.diglog.config;

import api.store.diglog.common.auth.CustomOAuth2User;
import api.store.diglog.model.dto.member.MemberInfoResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.test.context.support.WithSecurityContextFactory;
import org.springframework.stereotype.Component;

import java.util.Set;

@Component
public class WithMockCustomUserSecurityContextFactory implements WithSecurityContextFactory<WithMockCustomUser> {

    @Override
    public SecurityContext createSecurityContext(WithMockCustomUser annotation) {
        SecurityContext context = SecurityContextHolder.createEmptyContext();

        MemberInfoResponse dto = MemberInfoResponse.builder()
                .email(annotation.email())
                .username(annotation.username())
                .roles(Set.of(annotation.role(), "USER"))
                .build();

        CustomOAuth2User customOAuth2User = new CustomOAuth2User(dto);

        Authentication authentication = new UsernamePasswordAuthenticationToken(customOAuth2User, null, customOAuth2User.getAuthorities());
        context.setAuthentication(authentication);

        return context;
    }
}
