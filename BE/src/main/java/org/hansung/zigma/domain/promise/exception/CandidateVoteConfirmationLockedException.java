package org.hansung.zigma.domain.promise.exception;

import org.hansung.zigma.global.exception.BaseException;

public class CandidateVoteConfirmationLockedException extends BaseException {
    public CandidateVoteConfirmationLockedException() {
        super(CandidateVoteErrorCode.CANDIDATE_VOTE_CONFIRMATION_LOCKED);
    }
}
