package org.hansung.zigma.domain.promise.exception;

import org.hansung.zigma.global.exception.BaseException;

public class CandidateAccessDeniedException extends BaseException {
    public CandidateAccessDeniedException() { super(CandidateErrorCode.CANDIDATE_ACCESS_DENIED); }
}
