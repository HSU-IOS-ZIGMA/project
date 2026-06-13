package org.hansung.zigma.domain.promise.exception;

import org.hansung.zigma.global.exception.BaseException;

public class PromiseRevoteNotAvailableException extends BaseException {
    public PromiseRevoteNotAvailableException() {
        super(PromiseRevoteErrorCode.PROMISE_REVOTE_NOT_AVAILABLE);
    }
}
