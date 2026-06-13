package org.hansung.zigma.test;

import org.hansung.zigma.global.jwt.CustomUserDetails;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/test")
public class TestController { // 임시

    // accessToken이 유효한지 확인 (인증 필요)
    @GetMapping("/auth")
    public Map<String, Object> auth(@AuthenticationPrincipal CustomUserDetails userDetails) {
        return Map.of(
                "userId", userDetails.getUser().getId(),
                "message", "토큰 인증 성공"
        );
    }
}
