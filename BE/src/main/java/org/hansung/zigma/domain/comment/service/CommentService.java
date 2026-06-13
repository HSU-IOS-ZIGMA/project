package org.hansung.zigma.domain.comment.service;

import org.hansung.zigma.domain.comment.web.dto.CommentCommand;
import org.hansung.zigma.domain.comment.web.dto.CommentCreateReq;
import org.hansung.zigma.domain.comment.web.dto.CommentListRes;
import org.hansung.zigma.domain.comment.web.dto.CommentRes;

public interface CommentService {
    // 코멘트 생성
    CommentRes createComment(Long userId, Long promiseId, CommentCreateReq req);
    // 코멘트 조회 (Bounding Box 조회)
    CommentListRes getCommentsWithinBounds(Long userId, Long promiseId, CommentCommand cond);
}
