package org.hansung.zigma.domain.promise.exception;

import org.hansung.zigma.global.exception.BaseException;

public class CandidateVoteMultipleNotAllowedException extends BaseException {
    public CandidateVoteMultipleNotAllowedException() {
        super(CandidateVoteErrorCode.CANDIDATE_VOTE_MULTIPLE_NOT_ALLOWED);
    }
}
