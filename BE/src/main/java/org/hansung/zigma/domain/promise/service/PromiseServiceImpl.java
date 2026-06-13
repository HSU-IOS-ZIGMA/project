package org.hansung.zigma.domain.promise.service;

import lombok.RequiredArgsConstructor;
import org.hansung.zigma.domain.promise.entity.Promise;
import org.hansung.zigma.domain.promise.entity.PromiseMember;
import org.hansung.zigma.domain.promise.entity.Role;
import org.hansung.zigma.domain.promise.exception.PromiseMemberAccessDeniedException;
import org.hansung.zigma.domain.promise.exception.PromiseMemberHostOnlyException;
import org.hansung.zigma.domain.promise.exception.PromiseNotFoundException;
import org.hansung.zigma.domain.promise.repository.PromiseMemberRepository;
import org.hansung.zigma.domain.promise.repository.PromiseRepository;
import org.hansung.zigma.domain.promise.util.CursorUtil;
import org.hansung.zigma.domain.promise.util.CursorUtil.CursorContents;
import org.hansung.zigma.domain.promise.util.Validator;
import org.hansung.zigma.domain.promise.web.dto.PromiseCreateReq;
import org.hansung.zigma.domain.promise.web.dto.PromiseDetailRes;
import org.hansung.zigma.domain.promise.web.dto.PromiseInviteRes;
import org.hansung.zigma.domain.promise.web.dto.PromiseListRes;
import org.hansung.zigma.domain.promise.web.dto.PromiseRes;
import org.hansung.zigma.domain.user.entity.User;
import org.hansung.zigma.domain.user.exception.UserNotFoundException;
import org.hansung.zigma.domain.user.repository.UserRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PromiseServiceImpl implements PromiseService {

    private final UserRepository userRepository;
    private final PromiseRepository promiseRepository;
    private final PromiseMemberRepository promiseMemberRepository;

    @Override
    @Transactional
    public PromiseRes createPromise(Long userId, PromiseCreateReq req) {
        User user = userRepository.findById(userId)
                .orElseThrow(UserNotFoundException::new);

        Promise promise = Promise.toEntity(req);
        Validator.validatePromiseDates(promise.getPromisedAt(), promise.getEndAt(), LocalDateTime.now());

        PromiseMember host = PromiseMember.createMember(user, promise, Role.HOST);
        promise.setPromiseMember(host);

        Promise savedPromise = promiseRepository.save(promise);

        return PromiseRes.of(savedPromise, userId);
    }

    @Override
    public PromiseListRes getPromises(Long userId, String encodedCursor, int size) {
        User user = userRepository.findById(userId)
                .orElseThrow(UserNotFoundException::new);

        CursorContents contents = CursorUtil.decodeCursor(encodedCursor);

        List<Promise> promises = promiseRepository.findScrollByUserId(
                userId,
                contents != null ? contents.promisedAt() : null,
                contents != null ? contents.id() : null,
                PageRequest.of(0, size + 1)
        );

        List<PromiseRes> res = promises.stream()
                .map(promise -> PromiseRes.of(promise, userId))
                .toList();

        return PromiseListRes.of(res, size);
    }

    @Override
    public PromiseDetailRes getPromise(Long userId, Long promiseId) {
        userRepository.findById(userId)
                .orElseThrow(UserNotFoundException::new);

        Promise promise = promiseRepository.findDetailById(promiseId)
                .orElseThrow(PromiseNotFoundException::new);

        promiseMemberRepository.findByUserIdAndPromiseId(userId, promiseId)
                .orElseThrow(PromiseMemberAccessDeniedException::new);

        return PromiseDetailRes.of(promise, userId);
    }

    @Override
    @Transactional
    public PromiseInviteRes createInviteCode(Long userId, Long promiseId) {
        userRepository.findById(userId)
                .orElseThrow(UserNotFoundException::new);

        PromiseMember promiseMember = promiseMemberRepository.findByUserIdAndPromiseId(userId, promiseId)
                .orElseThrow(PromiseMemberAccessDeniedException::new);

        if (promiseMember.getRole() != Role.HOST) {
            throw new PromiseMemberHostOnlyException();
        }

        String inviteCode = promiseMember.getPromise().issueInviteCode();

        return PromiseInviteRes.of(promiseId, inviteCode);
    }

    @Override
    @Transactional
    public void joinPromiseByInviteCode(Long userId, String inviteCode) {
        User user = userRepository.findById(userId)
                .orElseThrow(UserNotFoundException::new);

        Promise promise = promiseRepository.findByInviteCode(inviteCode)
                .orElseThrow(PromiseNotFoundException::new);

        // 이미 참여 중이면 중복 생성하지 않고 그대로 성공 처리
        if (promiseMemberRepository.findByUserIdAndPromiseId(userId, promise.getId()).isPresent()) {
            return;
        }

        PromiseMember promiseMember = PromiseMember.createMember(user, promise, Role.MEMBER);
        promise.setPromiseMember(promiseMember);
    }
}
