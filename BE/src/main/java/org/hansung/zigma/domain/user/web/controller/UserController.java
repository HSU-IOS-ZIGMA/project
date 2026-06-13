package org.hansung.zigma.domain.user.web.controller;

import lombok.RequiredArgsConstructor;
import org.hansung.zigma.domain.user.service.UserService;
import org.hansung.zigma.domain.user.web.dto.UserRes;
import org.hansung.zigma.global.jwt.CustomUserDetails;
import org.hansung.zigma.global.response.SuccessResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/users")
public class UserController {

    private final UserService userService;

    @GetMapping("/mypage")
    public ResponseEntity<SuccessResponse<UserRes>> getMyProfile(
            @AuthenticationPrincipal CustomUserDetails customUserDetails
    ) {
        Long userId = Long.parseLong(customUserDetails.getUsername());

        UserRes res = userService.getMyProfile(userId);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(SuccessResponse.from(res));
    }
}
