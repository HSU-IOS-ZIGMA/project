package org.hansung.zigma.domain.promise.repository;

import org.hansung.zigma.domain.promise.entity.Candidate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CandidateRepository extends JpaRepository<Candidate, Long> {

    List<Candidate> findAllByPromiseId(Long promiseId);

    List<Candidate> findAllByPromiseIdAndIsActiveTrue(Long promiseId);

    Optional<Candidate> findByIdAndPromiseId(Long candidateId, Long promiseId);

    long countByPromiseId(Long promiseId);
}
