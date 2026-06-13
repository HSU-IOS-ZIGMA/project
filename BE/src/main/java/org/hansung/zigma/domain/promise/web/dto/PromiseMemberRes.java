package org.hansung.zigma.domain.promise.web.dto;

import org.hansung.zigma.domain.promise.entity.PromiseMember;
import org.hansung.zigma.domain.promise.entity.Role;
import org.hansung.zigma.domain.user.entity.User;

public record PromiseMemberRes(
        Long userId,
        String nickName,
        String profileImageUrl,
        Role role,
        Boolean isSelf
) {
    public static PromiseMemberRes of(PromiseMember member, Long currentUserId) {
        User user = member.getUser();
        return new PromiseMemberRes(
                user.getId(),
                user.getNickName(),
                user.getProfileImageUrl(),
                member.getRole(),
                user.getId().equals(currentUserId)
        );
    }
}
