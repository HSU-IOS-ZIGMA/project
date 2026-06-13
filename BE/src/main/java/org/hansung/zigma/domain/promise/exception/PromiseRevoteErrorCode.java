package org.hansung.zigma.domain.promise.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.hansung.zigma.global.response.code.BaseResponseCode;

@Getter
@AllArgsConstructor
public enum PromiseRevoteErrorCode implements BaseResponseCode {

    PROMISE_REVOTE_NOT_AVAILABLE("PROMISE_REVOTE_409_1", 409, "재투표를 진행할 동점 후보가 없습니다.");

    private final String code;
    private final int httpStatus;
    private final String message;
}
