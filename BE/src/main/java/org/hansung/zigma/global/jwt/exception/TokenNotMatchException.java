package org.hansung.zigma.global.jwt.exception;

import org.hansung.zigma.global.exception.BaseException;
import org.hansung.zigma.global.response.code.ErrorResponseCode;

public class TokenNotMatchException extends BaseException {
    public TokenNotMatchException() { super(ErrorResponseCode.TOKEN_NOT_MATCH); }
}
