package api.store.diglog.common.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.filter.CorsFilter;

import api.store.diglog.common.auth.CustomAccessDeniedHandler;
import api.store.diglog.common.auth.CustomAuthenticationEntryPoint;
import api.store.diglog.common.auth.CustomOAuth2FailureHandler;
import api.store.diglog.common.auth.CustomOAuth2SuccessHandler;
import api.store.diglog.common.auth.CustomOAuth2UserService;
import api.store.diglog.common.auth.JWTFilter;
import api.store.diglog.common.auth.JWTUtil;
import lombok.RequiredArgsConstructor;

@EnableWebSecurity
@EnableMethodSecurity
@Configuration
@RequiredArgsConstructor
public class SecurityConfig {

	private final CorsFilter corsFilter;
	private final JWTUtil jwtUtil;
	private final CustomOAuth2UserService customOAuth2UserService;
	private final CustomOAuth2SuccessHandler customOAuth2SuccessHandler;
	private final CustomOAuth2FailureHandler customOAuth2FailureHandler;
	private final CustomAccessDeniedHandler customAccessDeniedHandler;
	private final CustomAuthenticationEntryPoint customAuthenticationEntryPoint;

	@Bean
	public PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}

	@Bean
	public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
		String[] swaggerApi = {"/swagger-ui/**", "/bus/v3/api-docs/**", "/v3/api-docs/**"};
		String[] memberApi = {"/api/member/login", "/api/member/logout", "/api/member/refresh", "/api/member/profile/*",
			"/api/member/profile/search/*", "/api/verify/**"};
		String[] postApi = {"/api/post/view/increment"};
		String[] postGetApi = {"/api/post", "/api/post/*", "/api/post/member/tag", "/api/post/view/*"};
		String[] commentGetApi = {"/api/comment"};
		String[] folderGetApi = {"/api/folders/**"};
		String[] tagGetApi = {"/api/tag/**"};
		String[] subscribeGetApi = {"/api/subscriptions/users/**", "/api/subscriptions/authors/**"};
		String[] healthCheckApi = {"/api/health-check", "/health-check"};

		http
			.authorizeHttpRequests(authorizeHttpRequests -> authorizeHttpRequests
				.requestMatchers(swaggerApi).permitAll()
				.requestMatchers(memberApi).permitAll()
				.requestMatchers(HttpMethod.POST, postApi).permitAll()
				.requestMatchers(HttpMethod.GET, postGetApi).permitAll()
				.requestMatchers(HttpMethod.GET, commentGetApi).permitAll()
				.requestMatchers(HttpMethod.GET, folderGetApi).permitAll()
				.requestMatchers(HttpMethod.GET, tagGetApi).permitAll()
				.requestMatchers(HttpMethod.GET, subscribeGetApi).permitAll()
				.requestMatchers(HttpMethod.GET, healthCheckApi).permitAll()
				.anyRequest().authenticated())

			.csrf(AbstractHttpConfigurer::disable)
			.formLogin(AbstractHttpConfigurer::disable)
			.httpBasic(AbstractHttpConfigurer::disable)

			.sessionManagement(
				sessionManagement -> sessionManagement.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

			.addFilterBefore(new JWTFilter(jwtUtil), UsernamePasswordAuthenticationFilter.class)
			.addFilterBefore(corsFilter, UsernamePasswordAuthenticationFilter.class)

			.oauth2Login(oauth2 -> oauth2
				.redirectionEndpoint(redirect -> redirect
					.baseUri("/api/login/oauth2/code/*"))
				.userInfoEndpoint(userinfo -> userinfo
					.userService(customOAuth2UserService))
				.successHandler(customOAuth2SuccessHandler)
				.failureHandler(customOAuth2FailureHandler))

			.exceptionHandling(exceptionHandling -> exceptionHandling
				.accessDeniedHandler(customAccessDeniedHandler)
				.authenticationEntryPoint(customAuthenticationEntryPoint));

		return http.build();
	}
}
