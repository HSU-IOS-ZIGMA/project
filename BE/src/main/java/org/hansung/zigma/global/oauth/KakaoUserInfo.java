package org.hansung.zigma.global.oauth;

import lombok.RequiredArgsConstructor;
import org.hansung.zigma.domain.user.entity.UserProvider;

import java.util.Map;

@RequiredArgsConstructor
public class KakaoUserInfo implements OAuth2UserInfo {

    private final Map<String, Object> attributes;

    @Override
    public UserProvider getProvider() {
        return UserProvider.KAKAO;
    }

    @Override
    public String getProviderId() {
        return String.valueOf(attributes.get("id"));
    }

    @Override
    public String getEmail() {
        Map<String, Object> kakaoAccount = getKakaoAccount();
        if (kakaoAccount == null || kakaoAccount.get("email") == null) {
            return getProviderId() + "@kakao.local";
        }
        return (String) kakaoAccount.get("email");
    }

    @Override
    public String getNickname() {
        Map<String, Object> kakaoAccount = getKakaoAccount();
        if (kakaoAccount != null) {
            Map<String, Object> profile = getProfile(kakaoAccount);
            if (profile != null && profile.get("nickname") != null) {
                return (String) profile.get("nickname");
            }
        }

        Map<String, Object> properties = getProperties();
        if (properties != null && properties.get("nickname") != null) {
            return (String) properties.get("nickname");
        }

        return "Kakao User";
    }

    @Override
    public String getProfileImageUrl() {
        Map<String, Object> kakaoAccount = getKakaoAccount();
        if (kakaoAccount == null) return null;

        Map<String, Object> profile = getProfile(kakaoAccount);
        if (profile == null) return null;
        return (String) profile.get("profile_image_url");
    }

    private Map<String, Object> getKakaoAccount() {
        return (Map<String, Object>) attributes.get("kakao_account");
    }

    private Map<String, Object> getProperties() {
        return (Map<String, Object>) attributes.get("properties");
    }

    private Map<String, Object> getProfile(Map<String, Object> kakaoAccount) {
        return (Map<String, Object>) kakaoAccount.get("profile");
    }
}
