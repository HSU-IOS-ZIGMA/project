package org.hansung.zigma.domain.promise.service;

import org.hansung.zigma.domain.promise.web.dto.CandidateCreateReq;
import org.hansung.zigma.domain.promise.web.dto.CandidateConfirmReq;
import org.hansung.zigma.domain.promise.web.dto.CandidateListRes;
import org.hansung.zigma.domain.promise.web.dto.CandidateRes;

public interface CandidateService {
    CandidateRes createCandidate(Long userId, Long promiseId, CandidateCreateReq req);

    CandidateListRes getCandidates(Long userId, Long promiseId);

    void deleteCandidate(Long userId, Long promiseId, Long candidateId);

    void confirmCandidate(Long userId, Long promiseId, CandidateConfirmReq req);

    void revoteCandidates(Long userId, Long promiseId);
}
