package org.hansung.zigma.domain.promise.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.hansung.zigma.global.response.code.BaseResponseCode;

@Getter
@AllArgsConstructor
public enum CandidateErrorCode implements BaseResponseCode {

    CANDIDATE_ACCESS_DENIED("CANDIDATE_403_1", 403, "후보지는 등록한 본인만 삭제할 수 있습니다."),
    CANDIDATE_NOT_FOUND("CANDIDATE_404_1", 404, "해당 후보지를 찾을 수 없습니다."),
    CANDIDATE_INACTIVE("CANDIDATE_409_1", 409, "현재 활성화된 후보지가 아닙니다."),
    CANDIDATE_ALREADY_CONFIRMED("CANDIDATE_409_2", 409, "이미 확정된 장소입니다.");

    private final String code;
    private final int httpStatus;
    private final String message;
}
