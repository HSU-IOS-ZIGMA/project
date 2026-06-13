package org.hansung.zigma.domain.promise.exception;

import org.hansung.zigma.global.exception.BaseException;

public class PromiseInvalidPromiseTimeException extends BaseException {
    public PromiseInvalidPromiseTimeException() { super(PromiseErrorCode.PROMISE_INVALID_PROMISE_TIME); }
}
