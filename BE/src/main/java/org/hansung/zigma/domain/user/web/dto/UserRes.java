package org.hansung.zigma.domain.user.web.dto;

import org.hansung.zigma.domain.user.entity.User;

public record UserRes(
        Long userId,
        String email,
        String nickname,
        String profileImageUrl,
        String bio,
        long joinedPromiseCount,  // 참여중인 약속 수
        long hostedPromiseCount    // 내가 만든 약속 수
) {
    public static UserRes of(
            User user,
            long joinedPromiseCount,
            long hostedPromiseCount
    ) {
        return new UserRes(
                user.getId(),
                user.getEmail(),
                user.getNickName(),
                user.getProfileImageUrl(),
                user.getBio(),
                joinedPromiseCount,
                hostedPromiseCount
        );
    }
}
