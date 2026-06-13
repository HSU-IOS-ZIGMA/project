package org.hansung.zigma.domain.promise.web.dto;

import org.hansung.zigma.domain.promise.entity.Promise;
import org.hansung.zigma.domain.promise.entity.PromiseStatus;
import org.hansung.zigma.domain.promise.entity.Role;

import java.time.LocalDateTime;
import java.time.format.TextStyle;
import java.util.List;
import java.util.Locale;

public record PromiseDetailRes(
        Long id,
        String title,
        PromiseStatus promiseStatus,
        LocalDateTime promisedAt,
        String dayOfWeek,
        Boolean isMultipleVoting,
        boolean isLeader,
        Integer memberCount,
        List<PromiseMemberRes> members
) {
    public static PromiseDetailRes of(Promise promise, Long currentUserId) {
        String dayOfWeek = promise.getPromisedAt()
                .getDayOfWeek()
                .getDisplayName(TextStyle.SHORT, Locale.KOREAN);

        boolean isLeader = promise.getPromiseMembers().stream()
                .anyMatch(member -> member.getRole() == Role.HOST
                        && member.getUser().getId().equals(currentUserId));

        List<PromiseMemberRes> members = promise.getPromiseMembers().stream()
                .map(member -> PromiseMemberRes.of(member, currentUserId))
                .toList();

        return new PromiseDetailRes(
                promise.getId(),
                promise.getTitle(),
                promise.getStatus(),
                promise.getPromisedAt(),
                dayOfWeek,
                promise.getIsMultipleVoting(),
                isLeader,
                promise.getPromiseMembers().size(),
                members
        );
    }
}