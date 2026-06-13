package org.hansung.zigma.global.config;

import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.hansung.zigma.global.exception.CustomAuthenticationEntryPoint;
import org.hansung.zigma.global.jwt.JwtAuthenticationFilter;
import org.hansung.zigma.global.oauth.CookieOAuth2AuthorizationRequestRepository;
import org.hansung.zigma.global.oauth.CustomOAuth2UserService;
import org.hansung.zigma.global.oauth.OAuth2LoginSuccessHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableMethodSecurity // 메서드 보안 활성화
@RequiredArgsConstructor
public class SecurityConfig {

    private static final Logger log = LoggerFactory.getLogger(SecurityConfig.class);

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final CustomAuthenticationEntryPoint customAuthenticationEntryPoint;
    private final CustomOAuth2UserService customOAuth2UserService;
    private final OAuth2LoginSuccessHandler oAuth2LoginSuccessHandler;
    private final CookieOAuth2AuthorizationRequestRepository cookieOAuth2AuthorizationRequestRepository;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .cors(Customizer.withDefaults())
                .csrf(csrf -> csrf.disable()) // CSRF 비활성화
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)) // 세션 비활성화
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll() // CORS 프리플라이트 요청 허용
                        .requestMatchers("/oauth2/**", "/login/oauth2/**").permitAll() // OAuth2 로그인 URL
                        .requestMatchers("/dev/auth/**").permitAll() // Local demo login
                        .requestMatchers("/presence-test.html").permitAll() // WebSocket test page
                        .requestMatchers("/ws", "/ws/**").permitAll() // WebSocket handshake URL
                        .anyRequest().authenticated() // 그 외 나머지는 인증 필요
                )
                .oauth2Login(oauth2 -> oauth2
                        .authorizationEndpoint(authorization -> authorization
                                .authorizationRequestRepository(cookieOAuth2AuthorizationRequestRepository)
                        )
                        .userInfoEndpoint(userInfo -> userInfo.userService(customOAuth2UserService))
                        .successHandler(oAuth2LoginSuccessHandler)
                        .failureHandler((request, response, exception) -> {
                            log.error("OAuth failure uri={}, query={}, cookies={}, message={}",
                                    request.getRequestURI(),
                                    request.getQueryString(),
                                    request.getHeader("Cookie"),
                                    exception.getMessage(),
                                    exception);
                            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, exception.getMessage());
                        })
                )
                .exceptionHandling(exception -> exception
                        .authenticationEntryPoint(customAuthenticationEntryPoint)) // 401, 토큰 만료
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

}
