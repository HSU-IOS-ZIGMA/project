package org.hansung.zigma.domain.user.exception;

import org.hansung.zigma.global.exception.BaseException;

public class UserNotFoundException extends BaseException {
    public UserNotFoundException() {
        super(UserErrorCode.USER_NOT_FOUND);
    }
}
