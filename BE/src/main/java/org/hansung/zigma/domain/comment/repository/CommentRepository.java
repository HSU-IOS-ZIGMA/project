package org.hansung.zigma.domain.comment.repository;

import org.hansung.zigma.domain.comment.entity.Comment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CommentRepository extends JpaRepository<Comment, Long> {

    @Query("SELECT c FROM Comment c JOIN FETCH c.user " +
            "WHERE c.promise.id = :promiseId " +
            "AND c.latitude BETWEEN :minLat AND :maxLat " +
            "AND c.longitude BETWEEN :minLng AND :maxLng " +
            "ORDER BY c.createdAt DESC"
    )
    List<Comment> findWithinBounds(
            @Param("promiseId") Long promiseId,
            @Param("minLat") Double minLat,
            @Param("maxLat") Double maxLat,
            @Param("minLng") Double minLng,
            @Param("maxLng") Double maxLng
    );
}
