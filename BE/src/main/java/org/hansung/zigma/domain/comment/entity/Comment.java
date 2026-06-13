package org.hansung.zigma.domain.comment.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hansung.zigma.domain.comment.web.dto.CommentCreateReq;
import org.hansung.zigma.domain.promise.entity.Promise;
import org.hansung.zigma.domain.user.entity.User;
import org.hansung.zigma.global.entity.BaseEntity;

@Entity
@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "comments")
public class Comment extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    // 지도상의 좌표 정보
    @Column(nullable = false)
    private Double latitude; // 위도

    @Column(nullable = false)
    private Double longitude; // 경도

    // 연관 관계 설정
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "promise_id", nullable = false)
    private Promise promise;

    // ------------------------------ 메서드 ------------------------------
    public static Comment createComment(CommentCreateReq req, User user, Promise promise) {
        return Comment.builder()
                .content(req.getContent())
                .latitude(req.getLatitude())
                .longitude(req.getLongitude())
                .user(user)
                .promise(promise)
                .build();
    }
}
