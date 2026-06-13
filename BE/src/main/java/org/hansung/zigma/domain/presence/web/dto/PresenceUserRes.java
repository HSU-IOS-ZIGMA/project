package org.hansung.zigma.domain.presence.web.dto;

import org.hansung.zigma.domain.user.entity.User;

public record PresenceUserRes(
        Long userId,
        String nickName,
        String profileImageUrl
) {
    public static PresenceUserRes from(User user) {
        return new PresenceUserRes(
                user.getId(),
                user.getNickName(),
                user.getProfileImageUrl()
        );
    }
}
