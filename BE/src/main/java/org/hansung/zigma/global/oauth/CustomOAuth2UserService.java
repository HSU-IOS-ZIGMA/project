package org.hansung.zigma.global.oauth;

import lombok.RequiredArgsConstructor;
import org.hansung.zigma.domain.user.entity.User;
import org.hansung.zigma.domain.user.entity.UserProvider;
import org.hansung.zigma.domain.user.repository.UserRepository;
import org.hansung.zigma.global.jwt.CustomUserDetails;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private final UserRepository userRepository;

    @Override
    @Transactional
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        // OAuth API로 사용자 정보 요청
        OAuth2User oAuth2User = super.loadUser(userRequest);

        // registrationId로 구분하여 OAuth2UserInfo 파싱
        String registrationId = userRequest.getClientRegistration().getRegistrationId();
        OAuth2UserInfo userInfo = getUserInfo(registrationId, oAuth2User.getAttributes());

        // 회원가입 또는 기존 회원 조회
        User user = getOrCreate(userInfo);

        // CustomUserDetails 반환 (SuccessHandler에서 JWT 발급 시 user 사용)
        return new CustomUserDetails(user, oAuth2User.getAttributes());
    }

    private OAuth2UserInfo getUserInfo(String registrationId, Map<String, Object> attributes) {
        return switch (registrationId.toLowerCase()) {
            case "kakao" -> new KakaoUserInfo(attributes);
            case "naver" -> new NaverUserInfo(attributes);
            default -> throw new OAuth2AuthenticationException("Unsupported social type: " + registrationId);
        };
    }

    private User getOrCreate(OAuth2UserInfo userInfo) {
        UserProvider provider = userInfo.getProvider();
        return userRepository.findByProviderAndProviderId(provider, userInfo.getProviderId())
                .orElseGet(() -> {
                    User user = User.create(
                            provider,
                            userInfo.getProviderId(),
                            userInfo.getEmail(),
                            userInfo.getNickname(),
                            userInfo.getProfileImageUrl()
                    );
                    return userRepository.save(user);
                });
    }
}
