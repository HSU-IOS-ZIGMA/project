package org.hansung.zigma.domain.promise.web.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.hansung.zigma.domain.promise.service.PromiseService;
import org.hansung.zigma.domain.promise.web.dto.PromiseCreateReq;
import org.hansung.zigma.domain.promise.web.dto.PromiseDetailRes;
import org.hansung.zigma.domain.promise.web.dto.PromiseInviteRes;
import org.hansung.zigma.domain.promise.web.dto.PromiseListRes;
import org.hansung.zigma.domain.promise.web.dto.PromiseRes;
import org.hansung.zigma.global.jwt.CustomUserDetails;
import org.hansung.zigma.global.response.SuccessResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/promises")
public class PromiseController {

    private final PromiseService promiseService;

    @PostMapping
    public ResponseEntity<SuccessResponse<PromiseRes>> createPromise(
            @AuthenticationPrincipal CustomUserDetails customUserDetails,
            @RequestBody @Valid PromiseCreateReq req
    ) {
        Long userId = Long.parseLong(customUserDetails.getUsername());

        PromiseRes res = promiseService.createPromise(userId, req);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(SuccessResponse.created(res));
    }

    @GetMapping
    public ResponseEntity<SuccessResponse<PromiseListRes>> getPromises(
            @AuthenticationPrincipal CustomUserDetails customUserDetails,
            @RequestParam(required = false) String cursor, // 무한 스크롤 커서
            @RequestParam(defaultValue = "10") int size    // 한 페이지당 개수
    ) {
        Long userId = Long.parseLong(customUserDetails.getUsername());

        PromiseListRes res = promiseService.getPromises(userId, cursor, size);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(SuccessResponse.from(res));
    }

    @GetMapping("/{promiseId}")
    public ResponseEntity<SuccessResponse<PromiseDetailRes>> getPromise(
            @AuthenticationPrincipal CustomUserDetails customUserDetails,
            @PathVariable Long promiseId
    ) {
        Long userId = Long.parseLong(customUserDetails.getUsername());

        PromiseDetailRes res = promiseService.getPromise(userId, promiseId);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(SuccessResponse.from(res));
    }

    @PostMapping("/{promiseId}/invite")
    public ResponseEntity<SuccessResponse<PromiseInviteRes>> createInviteCode(
            @AuthenticationPrincipal CustomUserDetails customUserDetails,
            @PathVariable Long promiseId
    ) {
        Long userId = Long.parseLong(customUserDetails.getUsername());

        PromiseInviteRes res = promiseService.createInviteCode(userId, promiseId);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(SuccessResponse.ok(res));
    }

    @PostMapping("/invite/{inviteCode}")
    public ResponseEntity<SuccessResponse<Void>> joinPromiseByInviteCode(
            @AuthenticationPrincipal CustomUserDetails customUserDetails,
            @PathVariable String inviteCode
    ) {
        Long userId = Long.parseLong(customUserDetails.getUsername());

        promiseService.joinPromiseByInviteCode(userId, inviteCode);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(SuccessResponse.ok(null));
    }
}
