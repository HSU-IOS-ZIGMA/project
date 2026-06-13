package org.hansung.zigma.domain.promise.web.dto;

import org.hansung.zigma.domain.promise.entity.Promise;
import org.hansung.zigma.domain.promise.entity.PromiseStatus;
import org.hansung.zigma.domain.promise.entity.Role;

import java.time.LocalDateTime;
import java.time.format.TextStyle;
import java.util.Locale;

public record PromiseRes(
        Long id,
        String title,
        PromiseStatus promiseStatus,
        LocalDateTime promisedAt,
        String dayOfWeek,
        Integer memberCount,
        boolean isLeader
) {
    public static PromiseRes of(Promise promise, Long currentUserId) {
        String dayOfWeek = promise.getPromisedAt()
                .getDayOfWeek()
                .getDisplayName(TextStyle.SHORT, Locale.KOREAN);

        boolean isLeader = promise.getPromiseMembers().stream()
                .anyMatch(member -> member.getRole() == Role.HOST
                        && member.getUser().getId().equals(currentUserId));

        return new PromiseRes(
                promise.getId(),
                promise.getTitle(),
                promise.getStatus(),
                promise.getPromisedAt(),
                dayOfWeek,
                promise.getPromiseMembers().size(),
                isLeader
        );
    }
}
