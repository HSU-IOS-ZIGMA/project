package org.hansung.zigma.domain.promise.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.hansung.zigma.global.response.code.BaseResponseCode;

@Getter
@AllArgsConstructor
public enum PromiseErrorCode implements BaseResponseCode {

    PROMISE_CATEGORY_INVALID("PROMISE_400_1", 400, "유효하지 않은 카테고리 값입니다."),
    PROMISE_INVALID_PROMISE_TIME("PROMISE_400_2", 400, "약속 날짜는 현재 시간보다 미래여야 합니다."),
    PROMISE_INVALID_VOTING_TIME("PROMISE_400_3", 400, "투표 종료 시간은 현재 시간보다 미래여야 합니다."),
    PROMISE_INVALID_TIME_SEQUENCE("PROMISE_400_4", 400, "투표 종료 시간은 약속 시간보다 이전이어야 합니다."),
    PROMISE_VOTING_CLOSED("PROMISE_400_5", 400, "투표가 이미 마감된 약속입니다."),
    PROMISE_ALREADY_CONFIRMED("PROMISE_409_1", 409, "이미 장소가 확정된 약속입니다."),
    PROMISE_NOT_FOUND("PROMISE_404_1", 404, "존재하지 않는 약속입니다.");

    private final String code;
    private final int httpStatus;
    private final String message;
}
