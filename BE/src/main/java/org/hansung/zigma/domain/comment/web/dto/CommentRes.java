package org.hansung.zigma.domain.comment.web.dto;

import org.hansung.zigma.domain.comment.entity.Comment;

import java.time.LocalDateTime;

public record CommentRes(
        Long id,                // 코멘트 식별자
        Long userId,            // 작성자 식별자
        String nickname,        // 작성자 닉네임
        String profileImageUrl, // 작성자 프로필 사진
        String content,         // 코멘트 내용
        Double latitude,        // 위도
        Double longitude,       // 경도
        LocalDateTime createdAt // 생성 시간
) {
    public static CommentRes from(Comment comment) {
        return new CommentRes(
                comment.getId(),
                comment.getUser().getId(),
                comment.getUser().getNickName(),
                comment.getUser().getProfileImageUrl(),
                comment.getContent(),
                comment.getLatitude(),
                comment.getLongitude(),
                comment.getCreatedAt()
        );
    }
}
