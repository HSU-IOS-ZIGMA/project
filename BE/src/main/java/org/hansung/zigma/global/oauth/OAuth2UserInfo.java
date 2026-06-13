package org.hansung.zigma.global.oauth;

import org.hansung.zigma.domain.user.entity.UserProvider;

public interface OAuth2UserInfo {
    UserProvider getProvider();
    String getProviderId();
    String getEmail();
    String getNickname();
    String getProfileImageUrl();
}
