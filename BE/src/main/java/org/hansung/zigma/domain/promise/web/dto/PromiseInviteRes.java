package org.hansung.zigma.domain.promise.web.dto;

public record PromiseInviteRes(
        Long promiseId,
        String inviteCode
) {
    public static PromiseInviteRes of(Long promiseId, String inviteCode) {
        return new PromiseInviteRes(promiseId, inviteCode);
    }
}
