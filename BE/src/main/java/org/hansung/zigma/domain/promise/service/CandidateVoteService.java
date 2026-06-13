package org.hansung.zigma.domain.promise.service;

import org.hansung.zigma.domain.promise.web.dto.CandidateVoteCreateReq;

public interface CandidateVoteService {

    void createVote(Long userId, Long promiseId, CandidateVoteCreateReq req);

    void cancelVote(Long userId, Long promiseId, Long candidateId);
}
