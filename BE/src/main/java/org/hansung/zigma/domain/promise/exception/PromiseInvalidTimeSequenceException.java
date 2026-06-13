package org.hansung.zigma.domain.promise.exception;

import org.hansung.zigma.global.exception.BaseException;

public class PromiseInvalidTimeSequenceException extends BaseException {
    public PromiseInvalidTimeSequenceException() { super(PromiseErrorCode.PROMISE_INVALID_TIME_SEQUENCE); }
}
