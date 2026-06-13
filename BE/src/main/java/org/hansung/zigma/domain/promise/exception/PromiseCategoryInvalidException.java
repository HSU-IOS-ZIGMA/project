package org.hansung.zigma.domain.promise.exception;

import org.hansung.zigma.global.exception.BaseException;

public class PromiseCategoryInvalidException extends BaseException {
    public PromiseCategoryInvalidException() { super(PromiseErrorCode.PROMISE_CATEGORY_INVALID); }
}
