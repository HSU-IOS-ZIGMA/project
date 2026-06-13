package org.hansung.zigma.domain.promise.exception;

import org.hansung.zigma.global.exception.BaseException;

public class PromiseVotingClosedException extends BaseException {
    public PromiseVotingClosedException() {
        super(PromiseErrorCode.PROMISE_VOTING_CLOSED);
    }
}
