package org.hansung.zigma.global.config;

import lombok.RequiredArgsConstructor;
import org.hansung.zigma.domain.presence.service.PresenceService;
import org.hansung.zigma.domain.user.entity.User;
import org.hansung.zigma.global.jwt.CustomUserDetails;
import org.hansung.zigma.global.jwt.JwtTokenUtil;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
@RequiredArgsConstructor
public class StompAuthChannelInterceptor implements ChannelInterceptor {

    private static final Pattern PRESENCE_TOPIC_PATTERN =
            Pattern.compile("^/topic/promises/(\\d+)/presence$");

    private final JwtTokenUtil jwtTokenUtil;
    private final PresenceService presenceService;

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);
        if (accessor == null || accessor.getCommand() == null) {
            return message;
        }

        // WebSocket handshake 이후, STOMP CONNECT 프레임에서 JWT를 검증해 Principal을 만든다.
        if (StompCommand.CONNECT.equals(accessor.getCommand())) {
            authenticate(accessor);
        }

        // presence topic 구독은 약속 멤버만 가능해야 하므로 SUBSCRIBE 시점에 권한을 확인한다.
        if (StompCommand.SUBSCRIBE.equals(accessor.getCommand())) {
            validateSubscription(accessor);
        }

        return message;
    }

    private void authenticate(StompHeaderAccessor accessor) {
        String token = resolveToken(accessor.getFirstNativeHeader("Authorization"));
        jwtTokenUtil.validateTokenThrows(token);

        Long userId = Long.valueOf(jwtTokenUtil.getUserId(token));
        User user = User.builder()
                .id(userId)
                .build();
        CustomUserDetails userDetails = new CustomUserDetails(user);
        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());

        // 이후 @MessageMapping 메서드의 Principal에서 userId를 꺼낼 수 있게 저장한다.
        accessor.setUser(authentication);
    }

    private void validateSubscription(StompHeaderAccessor accessor) {
        String destination = accessor.getDestination();
        if (!StringUtils.hasText(destination)) {
            return;
        }

        Matcher matcher = PRESENCE_TOPIC_PATTERN.matcher(destination);
        if (!matcher.matches()) {
            return;
        }

        if (accessor.getUser() == null) {
            throw new IllegalArgumentException("인증되지 않은 WebSocket 요청입니다.");
        }

        Long promiseId = Long.valueOf(matcher.group(1));
        Long userId = Long.valueOf(accessor.getUser().getName());
        // destination에 포함된 promiseId 기준으로 DB의 약속 멤버 여부를 확인한다.
        presenceService.validatePromiseMember(promiseId, userId);
    }

    private String resolveToken(String authorizationHeader) {
        if (!StringUtils.hasText(authorizationHeader) || !authorizationHeader.startsWith("Bearer ")) {
            throw new IllegalArgumentException("Authorization 헤더가 필요합니다.");
        }

        return authorizationHeader.substring(7);
    }
}
