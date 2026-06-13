package org.hansung.zigma.domain.promise.exception;

import org.hansung.zigma.global.exception.BaseException;

public class PromiseMemberAccessDeniedException extends BaseException {
    public PromiseMemberAccessDeniedException() {
        super(PromiseMemberErrorCode.PROMISE_MEMBER_ACCESS_DENIED);
    }
}
