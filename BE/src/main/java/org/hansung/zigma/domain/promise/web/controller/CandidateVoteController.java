package org.hansung.zigma.domain.promise.web.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.hansung.zigma.domain.promise.service.CandidateVoteService;
import org.hansung.zigma.domain.promise.web.dto.CandidateVoteCreateReq;
import org.hansung.zigma.global.jwt.CustomUserDetails;
import org.hansung.zigma.global.response.SuccessResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/promises")
public class CandidateVoteController {

    private final CandidateVoteService candidateVoteService;


    // 투표하기
    @PostMapping("/{promiseId}/votes")
    public ResponseEntity<SuccessResponse<Void>> createVote(
            @AuthenticationPrincipal CustomUserDetails customUserDetails,
            @PathVariable Long promiseId,
            @RequestBody @Valid CandidateVoteCreateReq req
    ) {
        Long userId = Long.parseLong(customUserDetails.getUsername());

        candidateVoteService.createVote(userId, promiseId, req);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(SuccessResponse.created(null));
    }

    @DeleteMapping("/{promiseId}/votes/{candidateId}")
    public ResponseEntity<SuccessResponse<Void>> cancelVote(
            @AuthenticationPrincipal CustomUserDetails customUserDetails,
            @PathVariable Long promiseId,
            @PathVariable Long candidateId
    ) {
        Long userId = Long.parseLong(customUserDetails.getUsername());

        candidateVoteService.cancelVote(userId, promiseId, candidateId);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(SuccessResponse.ok(null));
    }
}
