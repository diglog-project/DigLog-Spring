package api.store.diglog.config;

import api.store.diglog.common.auth.*;
import api.store.diglog.common.config.SecurityConfig;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.web.filter.CorsFilter;

@Configuration
public class CustomSecurityConfiguration {

    @MockitoBean
    CorsFilter corsFilter;
    @MockitoBean
    JWTUtil jwtUtil;
    @MockitoBean
    CustomOAuth2UserService customOAuth2UserService;
    @MockitoBean
    CustomOAuth2SuccessHandler customOAuth2SuccessHandler;
    @MockitoBean
    CustomOAuth2FailureHandler customOAuth2FailureHandler;
    @MockitoBean
    CustomAccessDeniedHandler customAccessDeniedHandler;
    @MockitoBean
    CustomAuthenticationEntryPoint customAuthenticationEntryPoint;

    @Bean
    public SecurityFilterChain customSecurityFilterChain(HttpSecurity http) throws Exception {
        SecurityConfig securityConfig = new SecurityConfig(
                corsFilter,
                jwtUtil,
                customOAuth2UserService,
                customOAuth2SuccessHandler,
                customOAuth2FailureHandler,
                customAccessDeniedHandler,
                customAuthenticationEntryPoint
        );

        return securityConfig.filterChain(http);

    }
}
