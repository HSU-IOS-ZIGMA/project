package org.hansung.zigma.domain.promise.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.hansung.zigma.global.response.code.BaseResponseCode;

@Getter
@AllArgsConstructor
public enum CandidateVoteErrorCode implements BaseResponseCode {

    CANDIDATE_VOTE_DUPLICATED("CANDIDATE_VOTE_409_1", 409, "이미 해당 후보지에 투표했습니다."),
    CANDIDATE_VOTE_MULTIPLE_NOT_ALLOWED("CANDIDATE_VOTE_409_2", 409, "복수 투표가 허용되지 않은 약속입니다."),
    CANDIDATE_VOTE_CONFIRMATION_LOCKED("CANDIDATE_VOTE_409_3", 409, "장소가 이미 확정되어 더 이상 투표할 수 없습니다."),
    CANDIDATE_VOTE_NOT_FOUND("CANDIDATE_VOTE_404_1", 404, "내가 투표한 기록이 없습니다.");

    private final String code;
    private final int httpStatus;
    private final String message;
}
