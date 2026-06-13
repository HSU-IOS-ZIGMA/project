package org.hansung.zigma.global.oauth;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.hansung.zigma.global.jwt.CustomUserDetails;
import org.hansung.zigma.global.jwt.JwtTokenProvider;
import org.hansung.zigma.global.util.CookieUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;


@Component
@RequiredArgsConstructor
public class OAuth2LoginSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private static final Logger log = LoggerFactory.getLogger(OAuth2LoginSuccessHandler.class);

    private final JwtTokenProvider jwtTokenProvider;

    @Value("${app.oauth2.front-redirect-uri.local}")
    private String localRedirectUri;

    @Value("${app.oauth2.front-redirect-uri.deploy}")
    private String deployRedirectUri;

    @Value("${app.oauth2.front-redirect-uri.ios}")
    private String iosRedirectUri;

    @Override
    public void onAuthenticationSuccess(
            HttpServletRequest request,
            HttpServletResponse response,
            Authentication authentication) throws IOException {
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();

        String accessToken = jwtTokenProvider.createAccessToken(
                userDetails.getUser().getId().toString()
        );

        // 출발지(local/deploy) 쿠키 보고 리다이렉트 URI 선택, 누락 시 deploy 폴백
        String target = CookieUtils.getCookie(request,
                        CookieOAuth2AuthorizationRequestRepository.REDIRECT_TARGET_COOKIE)
                .map(Cookie::getValue)
                .orElse(CookieOAuth2AuthorizationRequestRepository.TARGET_DEPLOY);
String redirectUri = switch (target) {
    case CookieOAuth2AuthorizationRequestRepository.TARGET_IOS -> iosRedirectUri;
    case CookieOAuth2AuthorizationRequestRepository.TARGET_LOCAL -> localRedirectUri;
    default -> deployRedirectUri;
};

        CookieUtils.deleteCookie(request, response,
                CookieOAuth2AuthorizationRequestRepository.REDIRECT_TARGET_COOKIE);

        String targetUrl = UriComponentsBuilder.fromUriString(redirectUri)
                .queryParam("accessToken", accessToken)
                .build().toUriString();

        log.info("OAuth success target={}, redirectUri={}, tokenPrefix={}", target, redirectUri, accessToken.substring(0, Math.min(18, accessToken.length())));

        clearAuthenticationAttributes(request); // 인증 과정에서 세션에 임시 저장된 것 정리
        getRedirectStrategy().sendRedirect(request, response, targetUrl);
    }
}
