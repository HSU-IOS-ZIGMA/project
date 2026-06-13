package org.hansung.zigma.domain.promise.exception;

import org.hansung.zigma.global.exception.BaseException;

public class CandidateNotFoundException extends BaseException {
    public CandidateNotFoundException() {
        super(CandidateErrorCode.CANDIDATE_NOT_FOUND);
    }
}
