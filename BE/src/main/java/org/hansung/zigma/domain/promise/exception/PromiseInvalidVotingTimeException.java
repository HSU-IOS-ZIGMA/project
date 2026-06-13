package org.hansung.zigma.domain.promise.exception;

import org.hansung.zigma.global.exception.BaseException;

public class PromiseInvalidVotingTimeException extends BaseException {
    public PromiseInvalidVotingTimeException() { super(PromiseErrorCode.PROMISE_INVALID_VOTING_TIME); }
}
