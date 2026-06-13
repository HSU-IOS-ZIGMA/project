package org.hansung.zigma.domain.promise.web.dto;

import org.hansung.zigma.domain.promise.entity.Candidate;

public record CandidateRes(
        Long id,
        String name,
        String category,
        Double latitude,    // 위도
        Double longitude,   // 경도
        String address,
        Double distance,
        Boolean isConfirmed,
        CandidateVoteRes voteInfo
) {
    public static CandidateRes of(Candidate candidate, Long currentUserId) {
        return new CandidateRes(
                candidate.getId(),
                candidate.getName(),
                candidate.getCategory(),
                candidate.getLatitude(),
                candidate.getLongitude(),
                candidate.getAddress(),
                120.0,
                candidate.getIsConfirmed(),
                CandidateVoteRes.of(candidate, currentUserId)
        );
    }
}
