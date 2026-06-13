package org.hansung.zigma.domain.promise.exception;

import org.hansung.zigma.global.exception.BaseException;

public class PromiseAlreadyConfirmedException extends BaseException {
    public PromiseAlreadyConfirmedException() {
        super(PromiseErrorCode.PROMISE_ALREADY_CONFIRMED);
    }
}
