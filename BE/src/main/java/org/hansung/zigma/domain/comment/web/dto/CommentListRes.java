 package org.hansung.zigma.domain.comment.web.dto;

import java.util.List;

public record CommentListRes(
        List<CommentRes> comments,
        Integer count
) {
    public static CommentListRes from(List<CommentRes> comments) {
        return new CommentListRes(
                comments,
                comments.size()
        );
    }
}
