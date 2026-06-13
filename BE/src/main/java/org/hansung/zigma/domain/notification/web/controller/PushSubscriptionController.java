package org.hansung.zigma.domain.notification.web.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.hansung.zigma.domain.notification.config.WebPushProperties;
import org.hansung.zigma.domain.notification.service.PushSubscriptionService;
import org.hansung.zigma.domain.notification.web.dto.PushSubscriptionReq;
import org.hansung.zigma.domain.notification.web.dto.WebPushPublicKeyRes;
import org.hansung.zigma.global.jwt.CustomUserDetails;
import org.hansung.zigma.global.response.SuccessResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/notifications")
public class PushSubscriptionController {

    private final PushSubscriptionService pushSubscriptionService;
    private final WebPushProperties webPushProperties;

    @GetMapping("/web-push/public-key")
    public ResponseEntity<SuccessResponse<WebPushPublicKeyRes>> getWebPushPublicKey() {
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(SuccessResponse.ok(new WebPushPublicKeyRes(webPushProperties.getVapidPublicKey())));
    }

    @PostMapping("/push-subscriptions")
    public ResponseEntity<SuccessResponse<Void>> subscribe(
            @AuthenticationPrincipal CustomUserDetails customUserDetails,
            @RequestHeader(value = "User-Agent", required = false) String userAgent,
            @RequestBody @Valid PushSubscriptionReq req
    ) {
        Long userId = Long.parseLong(customUserDetails.getUsername());

        pushSubscriptionService.subscribe(userId, req, userAgent);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(SuccessResponse.created(null));
    }

    @DeleteMapping("/push-subscriptions")
    public ResponseEntity<SuccessResponse<?>> unsubscribe(
            @AuthenticationPrincipal CustomUserDetails customUserDetails,
            @RequestBody @Valid PushSubscriptionReq req
    ) {
        Long userId = Long.parseLong(customUserDetails.getUsername());

        pushSubscriptionService.unsubscribe(userId, req);

        return ResponseEntity
                .status(HttpStatus.NO_CONTENT)
                .body(SuccessResponse.noContent());
    }
}
