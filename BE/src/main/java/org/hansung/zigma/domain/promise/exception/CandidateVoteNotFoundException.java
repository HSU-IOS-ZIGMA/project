package org.hansung.zigma.domain.promise.exception;

import org.hansung.zigma.global.exception.BaseException;

public class CandidateVoteNotFoundException extends BaseException {
    public CandidateVoteNotFoundException() {
        super(CandidateVoteErrorCode.CANDIDATE_VOTE_NOT_FOUND);
    }
}
