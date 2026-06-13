package org.hansung.zigma.domain.promise.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hansung.zigma.domain.user.entity.User;
import org.hansung.zigma.global.entity.BaseEntity;

@Entity
@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(
        name = "candidate_votes",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_candidate_vote_user_candidate",
                        columnNames = {"user_id", "candidate_id"}
                )
        }
)
public class CandidateVote extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "candidate_id", nullable = false)
    @Setter(AccessLevel.PACKAGE)
    private Candidate candidate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "promise_id", nullable = false)
    @Setter(AccessLevel.PACKAGE)
    private Promise promise;

    public static CandidateVote createVote(User user, Candidate candidate) {
        return CandidateVote.builder()
                .user(user)
                .candidate(candidate)
                .promise(candidate.getPromise())
                .build();
    }
}
