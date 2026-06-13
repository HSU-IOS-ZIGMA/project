package org.hansung.zigma.domain.promise.repository;

import org.hansung.zigma.domain.promise.entity.CandidateVote;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CandidateVoteRepository extends JpaRepository<CandidateVote, Long> {

    Optional<CandidateVote> findByUserIdAndCandidateId(Long userId, Long candidateId);

    List<CandidateVote> findAllByPromiseId(Long promiseId);

    boolean existsByUserIdAndPromiseId(Long userId, Long promiseId);

    @Modifying(flushAutomatically = true)
    @Query("delete from CandidateVote cv where cv.promise.id = :promiseId")
    void deleteAllByPromiseId(@Param("promiseId") Long promiseId);
}
