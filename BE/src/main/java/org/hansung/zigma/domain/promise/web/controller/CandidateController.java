package org.hansung.zigma.domain.promise.web.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.hansung.zigma.domain.promise.service.CandidateService;
import org.hansung.zigma.domain.promise.web.dto.CandidateConfirmReq;
import org.hansung.zigma.domain.promise.web.dto.CandidateCreateReq;
import org.hansung.zigma.domain.promise.web.dto.CandidateListRes;
import org.hansung.zigma.domain.promise.web.dto.CandidateRes;
import org.hansung.zigma.global.jwt.CustomUserDetails;
import org.hansung.zigma.global.response.SuccessResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/promises")
public class CandidateController {

    private final CandidateService candidateService;

    @PostMapping("/{promiseId}/candidates")
    public ResponseEntity<SuccessResponse<CandidateRes>> createCandidate(
            @AuthenticationPrincipal CustomUserDetails customUserDetails,
            @PathVariable Long promiseId,
            @RequestBody @Valid CandidateCreateReq req
    ) {
        Long userId = Long.parseLong(customUserDetails.getUsername());

        CandidateRes res = candidateService.createCandidate(userId, promiseId, req);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(SuccessResponse.created(res));
    }

    @GetMapping("/{promiseId}/candidates")
    public ResponseEntity<SuccessResponse<CandidateListRes>> getCandidates(
            @AuthenticationPrincipal CustomUserDetails customUserDetails,
            @PathVariable Long promiseId
    ) {
        Long userId = Long.parseLong(customUserDetails.getUsername());

        CandidateListRes res = candidateService.getCandidates(userId, promiseId);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(SuccessResponse.ok(res));
    }

    @DeleteMapping("/{promiseId}/candidates/{candidateId}")
    public ResponseEntity<SuccessResponse<?>> deleteCandidate(
            @AuthenticationPrincipal CustomUserDetails customUserDetails,
            @PathVariable Long promiseId,
            @PathVariable Long candidateId
    ) {
        Long userId = Long.parseLong(customUserDetails.getUsername());

        candidateService.deleteCandidate(userId, promiseId, candidateId);

        return ResponseEntity
                .status(HttpStatus.NO_CONTENT)
                .body(SuccessResponse.noContent());
    }

    @PostMapping("/{promiseId}/confirmed")
    public ResponseEntity<SuccessResponse<Void>> confirmCandidate(
            @AuthenticationPrincipal CustomUserDetails customUserDetails,
            @PathVariable Long promiseId,
            @RequestBody @Valid CandidateConfirmReq req
    ) {
        Long userId = Long.parseLong(customUserDetails.getUsername());

        candidateService.confirmCandidate(userId, promiseId, req);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(SuccessResponse.ok(null));
    }

    @PostMapping("/{promiseId}/revote")
    public ResponseEntity<SuccessResponse<Void>> revoteCandidates(
            @AuthenticationPrincipal CustomUserDetails customUserDetails,
            @PathVariable Long promiseId
    ) {
        Long userId = Long.parseLong(customUserDetails.getUsername());

        candidateService.revoteCandidates(userId, promiseId);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(SuccessResponse.ok(null));
    }
}
