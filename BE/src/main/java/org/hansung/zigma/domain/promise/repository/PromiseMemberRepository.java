package org.hansung.zigma.domain.promise.repository;

import org.hansung.zigma.domain.promise.entity.PromiseMember;
import org.hansung.zigma.domain.promise.entity.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PromiseMemberRepository extends JpaRepository<PromiseMember, Long> {

    Optional<PromiseMember> findByUserIdAndPromiseId(Long userId, Long promiseId);

    @Query("SELECT pm FROM PromiseMember pm " +
            "JOIN FETCH pm.user " +
            "WHERE pm.promise.id = :promiseId")
    List<PromiseMember> findAllByPromiseId(@Param("promiseId") Long promiseId);

    long countByUserId(Long userId);

    long countByUserIdAndRole(Long userId, Role role);

    int countByPromiseId(Long promiseId);
}
