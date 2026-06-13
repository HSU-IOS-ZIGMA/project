package org.hansung.zigma.global.jwt.exception;

import org.hansung.zigma.global.exception.BaseException;
import org.hansung.zigma.global.response.code.ErrorResponseCode;

public class TokenInvalidException extends BaseException {
    public TokenInvalidException() { super(ErrorResponseCode.INVALID_TOKEN); }
}
