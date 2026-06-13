package org.hansung.zigma.domain.promise.web.dto;

import org.hansung.zigma.domain.promise.entity.Candidate;
import org.hansung.zigma.domain.promise.entity.CandidateVote;
import org.hansung.zigma.domain.user.entity.User;

import java.util.List;

public record CandidateVoteRes(
        Creator creator,                 // 누가 만들었는지
        Integer voteCount,               // 투표한 사람 수
        List<Voter> voters,              // 누가 투표했는지
        Boolean isMyVote,                // 내가 투표했는지
        Boolean isMyCandidate            // 내가 추가한 후보지인지
) {

    public record Creator(
            Long userId,
            String nickname
    ) {
        public static Creator from(User user) {
            return new Creator(
                    user.getId(),
                    user.getNickName()
            );
        }
    }

    public record Voter(
            Long userId,
            String nickname
    ) {
        public static Voter from(CandidateVote vote) {
            User user = vote.getUser();
            return new Voter(
                    user.getId(),
                    user.getNickName()
            );
        }
    }

    public static CandidateVoteRes of(Candidate candidate, Long currentUserId) {
        List<Voter> voters = candidate.getCandidateVotes().stream()
                .map(Voter::from)
                .toList();

        boolean isMyVote = voters.stream()
                .anyMatch(v -> v.userId().equals(currentUserId));

        boolean isMyCandidate = candidate.getUser().getId().equals(currentUserId);

        return new CandidateVoteRes(
                Creator.from(candidate.getUser()),
                voters.size(),
                voters,
                isMyVote,
                isMyCandidate
        );
    }
}
