package org.hansung.zigma.domain.promise.exception;

import org.hansung.zigma.global.exception.BaseException;

public class CandidateInactiveException extends BaseException {
    public CandidateInactiveException() {
        super(CandidateErrorCode.CANDIDATE_INACTIVE);
    }
}
