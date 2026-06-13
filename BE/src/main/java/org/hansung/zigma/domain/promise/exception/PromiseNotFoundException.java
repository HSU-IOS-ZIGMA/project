package org.hansung.zigma.domain.promise.exception;

import org.hansung.zigma.global.exception.BaseException;

public class PromiseNotFoundException extends BaseException {
    public PromiseNotFoundException() { super(PromiseErrorCode.PROMISE_NOT_FOUND); }
}
