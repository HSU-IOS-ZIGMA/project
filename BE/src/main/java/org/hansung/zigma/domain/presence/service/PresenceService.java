package org.hansung.zigma.domain.presence.service;

import lombok.RequiredArgsConstructor;
import org.hansung.zigma.domain.presence.web.dto.PresenceUserRes;
import org.hansung.zigma.domain.promise.entity.PromiseMember;
import org.hansung.zigma.domain.promise.exception.PromiseMemberAccessDeniedException;
import org.hansung.zigma.domain.promise.repository.PromiseMemberRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
public class PresenceService {

    private final PromiseMemberRepository promiseMemberRepository;

    // promiseId -> sessionId -> online user. 같은 사용자가 여러 탭으로 접속해도 세션 단위로 추적한다.
    private final Map<Long, Map<String, PresenceUserRes>> usersByPromiseId = new ConcurrentHashMap<>();

    // disconnect 이벤트는 sessionId만 알려주므로, 해당 세션이 들어가 있던 약속들을 역방향으로 기록한다.
    private final Map<String, Set<Long>> promiseIdsBySessionId = new ConcurrentHashMap<>();

    @Transactional(readOnly = true)
    public List<PresenceUserRes> join(Long promiseId, Long userId, String sessionId) {
        // 접속자로 표시하기 전에 해당 유저가 약속 멤버인지 먼저 검증한다.
        PromiseMember member = promiseMemberRepository.findByUserIdAndPromiseId(userId, promiseId)
                .orElseThrow(PromiseMemberAccessDeniedException::new);

        PresenceUserRes user = PresenceUserRes.from(member.getUser());
        usersByPromiseId
                .computeIfAbsent(promiseId, key -> new ConcurrentHashMap<>())
                .put(sessionId, user);

        promiseIdsBySessionId
                .computeIfAbsent(sessionId, key -> ConcurrentHashMap.newKeySet())
                .add(promiseId);

        return getOnlineUsers(promiseId);
    }

    public List<PresenceUserRes> leave(Long promiseId, String sessionId) {
        removeSessionFromPromise(promiseId, sessionId);
        return getOnlineUsers(promiseId);
    }

    public List<Long> leaveAll(String sessionId) {
        Set<Long> promiseIds = promiseIdsBySessionId.remove(sessionId);
        if (promiseIds == null) {
            return List.of();
        }

        // 연결이 끊긴 세션이 참여 중이던 모든 약속에서 제거하고, 변경된 약속 ID를 반환한다.
        List<Long> changedPromiseIds = new ArrayList<>(promiseIds);
        changedPromiseIds.forEach(promiseId -> removeSessionFromPromise(promiseId, sessionId));
        return changedPromiseIds;
    }

    public List<PresenceUserRes> getOnlineUsers(Long promiseId) {
        Map<String, PresenceUserRes> usersBySessionId = usersByPromiseId.get(promiseId);
        if (usersBySessionId == null) {
            return List.of();
        }

        // 여러 탭으로 들어온 같은 유저는 프론트에 한 번만 내려준다.
        Map<Long, PresenceUserRes> uniqueUsers = new LinkedHashMap<>();
        usersBySessionId.values().forEach(user -> uniqueUsers.putIfAbsent(user.userId(), user));
        return List.copyOf(uniqueUsers.values());
    }

    @Transactional(readOnly = true)
    public void validatePromiseMember(Long promiseId, Long userId) {
        promiseMemberRepository.findByUserIdAndPromiseId(userId, promiseId)
                .orElseThrow(PromiseMemberAccessDeniedException::new);
    }

    private void removeSessionFromPromise(Long promiseId, String sessionId) {
        Map<String, PresenceUserRes> usersBySessionId = usersByPromiseId.get(promiseId);
        if (usersBySessionId != null) {
            usersBySessionId.remove(sessionId);
            if (usersBySessionId.isEmpty()) {
                usersByPromiseId.remove(promiseId);
            }
        }

        Set<Long> promiseIds = promiseIdsBySessionId.get(sessionId);
        if (promiseIds != null) {
            promiseIds.remove(promiseId);
            if (promiseIds.isEmpty()) {
                promiseIdsBySessionId.remove(sessionId);
            }
        }
    }
}
