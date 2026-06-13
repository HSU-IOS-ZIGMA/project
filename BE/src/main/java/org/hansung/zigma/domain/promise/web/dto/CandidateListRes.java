package org.hansung.zigma.domain.promise.web.dto;

import java.util.List;

public record CandidateListRes(
        Integer totalMemberCount,
        Integer candidateCount,
        List<CandidateRes> candidates
) {
    public static CandidateListRes of(List<CandidateRes> candidates, int totalMemberCount) {
        return new CandidateListRes(
                totalMemberCount,
                candidates.size(),
                candidates
        );
    }
}