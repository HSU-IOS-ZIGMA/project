package org.hansung.zigma.domain.promise.service;

import org.hansung.zigma.domain.promise.web.dto.PromiseCreateReq;
import org.hansung.zigma.domain.promise.web.dto.PromiseDetailRes;
import org.hansung.zigma.domain.promise.web.dto.PromiseInviteRes;
import org.hansung.zigma.domain.promise.web.dto.PromiseListRes;
import org.hansung.zigma.domain.promise.web.dto.PromiseRes;

public interface PromiseService {
    // 약속 생성
    PromiseRes createPromise(Long userId, PromiseCreateReq req);
    // 약속 전체 조회
    PromiseListRes getPromises(Long userId, String encodedCursor, int size);
    // 약속 단일 조회
    PromiseDetailRes getPromise(Long userId, Long promiseId);
    // 초대 코드 생성
    PromiseInviteRes createInviteCode(Long userId, Long promiseId);
    // 초대 코드로 멤버 참여
    void joinPromiseByInviteCode(Long userId, String inviteCode);
}
