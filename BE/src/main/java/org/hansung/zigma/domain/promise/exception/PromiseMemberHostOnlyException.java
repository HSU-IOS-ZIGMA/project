package org.hansung.zigma.domain.promise.exception;

import org.hansung.zigma.global.exception.BaseException;

public class PromiseMemberHostOnlyException extends BaseException {
    public PromiseMemberHostOnlyException() {
        super(PromiseMemberErrorCode.PROMISE_MEMBER_HOST_ONLY);
    }
}
