package org.hansung.zigma.domain.promise.exception;

import org.hansung.zigma.global.exception.BaseException;

public class CandidateVoteDuplicatedException extends BaseException {
    public CandidateVoteDuplicatedException() {
        super(CandidateVoteErrorCode.CANDIDATE_VOTE_DUPLICATED);
    }
}
