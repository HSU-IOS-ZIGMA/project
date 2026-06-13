package org.hansung.zigma.domain.comment.service;

import lombok.RequiredArgsConstructor;
import org.hansung.zigma.domain.comment.entity.Comment;
import org.hansung.zigma.domain.comment.repository.CommentRepository;
import org.hansung.zigma.domain.comment.web.dto.CommentCommand;
import org.hansung.zigma.domain.comment.web.dto.CommentCreateReq;
import org.hansung.zigma.domain.comment.web.dto.CommentListRes;
import org.hansung.zigma.domain.comment.web.dto.CommentRes;
import org.hansung.zigma.domain.promise.entity.PromiseMember;
import org.hansung.zigma.domain.promise.exception.PromiseMemberAccessDeniedException;
import org.hansung.zigma.domain.promise.repository.PromiseMemberRepository;
import org.hansung.zigma.domain.user.entity.User;
import org.hansung.zigma.domain.user.exception.UserNotFoundException;
import org.hansung.zigma.domain.user.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CommentServiceImpl implements CommentService {

    private final UserRepository userRepository;
    private final PromiseMemberRepository promiseMemberRepository;
    private final CommentRepository commentRepository;

    @Override
    @Transactional
    public CommentRes createComment(Long userId, Long promiseId, CommentCreateReq req) {
        User user = userRepository.findById(userId)
                .orElseThrow(UserNotFoundException::new);

        PromiseMember pm = promiseMemberRepository.findByUserIdAndPromiseId(userId, promiseId)
                .orElseThrow(PromiseMemberAccessDeniedException::new);

        Comment comment = Comment.createComment(req, user, pm.getPromise());
        Comment savedComment = commentRepository.save(comment);

        return CommentRes.from(savedComment);
    }

    @Override
    public CommentListRes getCommentsWithinBounds(Long userId, Long promiseId, CommentCommand cond) {
        userRepository.findById(userId)
                .orElseThrow(UserNotFoundException::new);

        promiseMemberRepository.findByUserIdAndPromiseId(userId, promiseId)
                .orElseThrow(PromiseMemberAccessDeniedException::new);

        List<Comment> comments = commentRepository.findWithinBounds(
                promiseId,
                cond.getMinLat(), cond.getMaxLat(),
                cond.getMinLng(), cond.getMaxLng()
        );

        List<CommentRes> res = comments.stream()
                .map(CommentRes::from)
                .toList();

        return CommentListRes.from(res);
    }
}
