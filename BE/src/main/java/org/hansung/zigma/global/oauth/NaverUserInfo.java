package org.hansung.zigma.global.oauth;

import lombok.RequiredArgsConstructor;
import org.hansung.zigma.domain.user.entity.UserProvider;

import java.util.Map;

@RequiredArgsConstructor
public class NaverUserInfo implements OAuth2UserInfo {

    private final Map<String, Object> attributes;

    @Override
    public UserProvider getProvider() {
        return UserProvider.NAVER;
    }

    @Override
    public String getProviderId() {
        Map<String, Object> response = getResponse();
        if (response == null) return null;
        return (String) response.get("id");
    }

    @Override
    public String getEmail() {
        Map<String, Object> response = getResponse();
        if (response == null) return null;
        return (String) response.get("email");
    }

    @Override
    public String getNickname() {
        Map<String, Object> response = getResponse();
        if (response == null) return null;
        return (String) response.get("nickname");
    }

    @Override
    public String getProfileImageUrl() {
        Map<String, Object> response = getResponse();
        if (response == null) return null;
        return (String) response.get("profile_image_url");
    }

    // ---------- 메서드 ----------
    private Map<String, Object> getResponse() {
        return (Map<String, Object>) attributes.get("response");
    }
}
