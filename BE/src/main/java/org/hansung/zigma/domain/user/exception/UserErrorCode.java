package org.hansung.zigma.domain.user.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.hansung.zigma.global.response.code.BaseResponseCode;

@Getter
@AllArgsConstructor
public enum UserErrorCode implements BaseResponseCode {

    USER_NOT_FOUND("USER_404_1", 404, "존재하지 않는 계정입니다.");

    private final String code;
    private final int httpStatus;
    private final String message;
}
