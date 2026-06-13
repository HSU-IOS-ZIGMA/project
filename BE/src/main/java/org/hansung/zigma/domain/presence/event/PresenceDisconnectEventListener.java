package org.hansung.zigma.domain.presence.event;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.hansung.zigma.domain.presence.service.PresenceService;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

@Component
@RequiredArgsConstructor
@Slf4j
public class PresenceDisconnectEventListener {

    private final PresenceService presenceService;
    private final SimpMessagingTemplate messagingTemplate;

    @EventListener
    public void handleSessionDisconnect(SessionDisconnectEvent event) {
        String sessionId = event.getSessionId();
        log.info("WebSocket session disconnected. sessionId={}", sessionId);

        // 탭 닫기, 새로고침, 네트워크 끊김처럼 leave 메시지가 오지 않는 경우도 여기서 정리한다.
        presenceService.leaveAll(sessionId)
                .forEach(promiseId -> messagingTemplate.convertAndSend(
                        "/topic/promises/" + promiseId + "/presence",
                        presenceService.getOnlineUsers(promiseId)
                ));
    }
}
