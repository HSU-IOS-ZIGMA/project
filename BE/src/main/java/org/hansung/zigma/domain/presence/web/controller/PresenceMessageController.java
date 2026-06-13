package org.hansung.zigma.domain.presence.web.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.hansung.zigma.domain.presence.service.PresenceService;
import org.hansung.zigma.domain.presence.web.dto.PresencePingReq;
import org.hansung.zigma.domain.presence.web.dto.PresencePingRes;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.security.Principal;

@Controller
@RequiredArgsConstructor
@Slf4j
public class PresenceMessageController {

    private final SimpMessagingTemplate messagingTemplate;
    private final PresenceService presenceService;

    @MessageMapping("/promises/{promiseId}/presence/join")
    public void join(
            @DestinationVariable Long promiseId,
            @Header("simpSessionId") String sessionId,
            Principal principal
    ) {
        // STOMP CONNECT 때 인증 인터셉터가 넣어둔 userId를 Principal에서 꺼낸다.
        Long userId = Long.valueOf(principal.getName());
        log.info("Presence join received. promiseId={}, userId={}, sessionId={}", promiseId, userId, sessionId);

        broadcastOnlineUsers(promiseId, presenceService.join(promiseId, userId, sessionId));
    }

    @MessageMapping("/promises/{promiseId}/presence/leave")
    public void leave(
            @DestinationVariable Long promiseId,
            @Header("simpSessionId") String sessionId
    ) {
        log.info("Presence leave received. promiseId={}, sessionId={}", promiseId, sessionId);

        broadcastOnlineUsers(promiseId, presenceService.leave(promiseId, sessionId));
    }

    @MessageMapping("/promises/{promiseId}/presence/ping")
    public void ping(
            @DestinationVariable Long promiseId,
            PresencePingReq request
    ) {
        log.info("Presence ping received. promiseId={}, message={}", promiseId, request.message());

        PresencePingRes response = PresencePingRes.from(promiseId, request);
        messagingTemplate.convertAndSend("/topic/promises/" + promiseId + "/presence", response);
        log.info("Presence ping broadcast. destination=/topic/promises/{}/presence", promiseId);
    }

    private void broadcastOnlineUsers(Long promiseId, Object onlineUsers) {
        String destination = "/topic/promises/" + promiseId + "/presence";
        // 같은 약속 presence topic을 구독 중인 클라이언트에게 최신 접속자 목록을 보낸다.
        messagingTemplate.convertAndSend(destination, onlineUsers);
        log.info("Presence users broadcast. destination={}", destination);
    }
}
