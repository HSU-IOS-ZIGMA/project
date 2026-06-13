package org.hansung.zigma.domain.promise.exception;

import org.hansung.zigma.global.exception.BaseException;

public class CandidateAlreadyConfirmedException extends BaseException {
    public CandidateAlreadyConfirmedException() {
        super(CandidateErrorCode.CANDIDATE_ALREADY_CONFIRMED);
    }
}
