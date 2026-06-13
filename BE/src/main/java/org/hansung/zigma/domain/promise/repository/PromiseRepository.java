package org.hansung.zigma.domain.promise.repository;

import org.hansung.zigma.domain.promise.entity.Promise;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface PromiseRepository extends JpaRepository<Promise, Long> {

    @Query("SELECT DISTINCT p FROM Promise p " +
            "JOIN FETCH p.promiseMembers pm " +
            "WHERE p.id IN (SELECT pm2.promise.id FROM PromiseMember pm2 WHERE pm2.user.id = :userId) " +
            "AND (:lastPromisedAt IS NULL OR " +
            "      (p.promisedAt < :lastPromisedAt) OR " +
            "      (p.promisedAt = :lastPromisedAt AND p.id < :cursor)) " +
            "ORDER BY p.promisedAt DESC, p.id DESC")
    List<Promise> findScrollByUserId(@Param("userId") Long userId,
                                     @Param("lastPromisedAt") LocalDateTime lastPromisedAt,
                                     @Param("cursor") Long cursor,
                                     Pageable pageable);

    @Query("SELECT p FROM Promise p " +
            "JOIN FETCH p.promiseMembers pm " +
            "JOIN FETCH pm.user " +
            "WHERE p.id = :promiseId")
    Optional<Promise> findDetailById(@Param("promiseId") Long promiseId);

    Optional<Promise> findByInviteCode(String inviteCode);
}
