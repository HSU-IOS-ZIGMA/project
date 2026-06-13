package org.hansung.zigma.domain.promise.service;

import lombok.RequiredArgsConstructor;
import org.hansung.zigma.domain.notification.event.PromiseConfirmedEvent;
import org.hansung.zigma.domain.promise.entity.Candidate;
import org.hansung.zigma.domain.promise.entity.CandidateVote;
import org.hansung.zigma.domain.promise.entity.PromiseStatus;
import org.hansung.zigma.domain.promise.entity.PromiseMember;
import org.hansung.zigma.domain.promise.entity.Role;
import org.hansung.zigma.domain.promise.exception.*;
import org.hansung.zigma.domain.promise.repository.CandidateRepository;
import org.hansung.zigma.domain.promise.repository.CandidateVoteRepository;
import org.hansung.zigma.domain.promise.repository.PromiseMemberRepository;
import org.hansung.zigma.domain.promise.web.dto.CandidateConfirmReq;
import org.hansung.zigma.domain.promise.repository.PromiseRepository;
import org.hansung.zigma.domain.promise.web.dto.CandidateCreateReq;
import org.hansung.zigma.domain.promise.web.dto.CandidateListRes;
import org.hansung.zigma.domain.promise.web.dto.CandidateRes;
import org.hansung.zigma.domain.user.exception.UserNotFoundException;
import org.hansung.zigma.domain.user.repository.UserRepository;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CandidateServiceImpl implements CandidateService {

    private final UserRepository userRepository;
    private final PromiseRepository promiseRepository;
    private final PromiseMemberRepository promiseMemberRepository;
    private final CandidateRepository candidateRepository;
    private final CandidateVoteRepository candidateVoteRepository;
    private final ApplicationEventPublisher eventPublisher;

    @Override
    @Transactional
    public CandidateRes createCandidate(Long userId, Long promiseId, CandidateCreateReq req) {
        userRepository.findById(userId)
                .orElseThrow(UserNotFoundException::new);

        promiseRepository.findById(promiseId)
                .orElseThrow(PromiseNotFoundException::new);

        PromiseMember pm = promiseMemberRepository.findByUserIdAndPromiseId(userId, promiseId)
                .orElseThrow(PromiseMemberAccessDeniedException::new);

        Candidate candidate = Candidate.createCandidate(
                req, pm.getUser(), pm.getPromise()
        );
        Candidate savedCandidate = candidateRepository.save(candidate);

        // 첫 후보지가 등록되면 약속 상태를 진행 중으로 전환
        pm.getPromise().proceed();

        return CandidateRes.of(savedCandidate, userId);
    }

    @Override
    public CandidateListRes getCandidates(Long userId, Long promiseId) {
        userRepository.findById(userId)
                .orElseThrow(UserNotFoundException::new);

        promiseRepository.findById(promiseId)
                .orElseThrow(PromiseNotFoundException::new);

        promiseMemberRepository.findByUserIdAndPromiseId(userId, promiseId)
                .orElseThrow(PromiseMemberAccessDeniedException::new);

        int totalMemberCount = (int) promiseMemberRepository.countByPromiseId(promiseId);

        // 현재 투표 대상으로 살아있는 후보지만 반환
        List<Candidate> candidates = candidateRepository.findAllByPromiseIdAndIsActiveTrue(promiseId);
        List<CandidateRes> res = candidates.stream()
                .map(c -> CandidateRes.of(c, userId))
                .toList();

        return CandidateListRes.of(res, totalMemberCount);
    }

    @Override
    @Transactional
    public void deleteCandidate(Long userId, Long promiseId, Long candidateId) {
        userRepository.findById(userId)
                .orElseThrow(UserNotFoundException::new);

        promiseRepository.findById(promiseId)
                .orElseThrow(PromiseNotFoundException::new);

        promiseMemberRepository.findByUserIdAndPromiseId(userId, promiseId)
                .orElseThrow(PromiseMemberAccessDeniedException::new);

        Candidate candidate = candidateRepository.findByIdAndPromiseId(candidateId, promiseId)
                .orElseThrow(CandidateNotFoundException::new);

        if (!candidate.getUser().getId().equals(userId)) {
            throw new CandidateAccessDeniedException();
        }

        if (candidate.getIsConfirmed()) {
            throw new CandidateAlreadyConfirmedException();
        }

        candidateRepository.delete(candidate);

        // 마지막 후보지 삭제 시 약속 상태를 다시 장소 미정으로 되돌림
        if (candidateRepository.countByPromiseId(promiseId) == 0) {
            candidate.getPromise().pend();
        }
    }

    @Override
    @Transactional
    public void confirmCandidate(Long userId, Long promiseId, CandidateConfirmReq req) {
        // 1. 인증된 사용자 자체가 유효한지 확인
        userRepository.findById(userId)
                .orElseThrow(UserNotFoundException::new);

        // 2. 해당 사용자가 이 약속의 참여자인지 확인
        PromiseMember promiseMember = promiseMemberRepository.findByUserIdAndPromiseId(userId, promiseId)
                .orElseThrow(PromiseMemberAccessDeniedException::new);

        // 3. 장소 확정은 방장만 가능
        if (promiseMember.getRole() != Role.HOST) {
            throw new PromiseMemberHostOnlyException();
        }

        // 4. 요청한 후보지가 실제로 이 약속에 속한 후보지인지 확인
        Candidate confirmedCandidate = candidateRepository.findByIdAndPromiseId(req.getCandidateId(), promiseId)
                .orElseThrow(CandidateNotFoundException::new);

        // 5. 현재 활성화된 후보지만 장소 확정 대상이 될 수 있음
        if (!confirmedCandidate.getIsActive()) {
            throw new CandidateInactiveException();
        }

        // 6. 이미 확정된 약속이면 중복 확정을 막음
        if (confirmedCandidate.getPromise().getStatus() == PromiseStatus.CONFIRMED) {
            throw new PromiseAlreadyConfirmedException();
        }

        // 7. 같은 약속의 후보지들을 모두 미확정 처리한 뒤
        //    선택한 후보지만 확정 상태로 변경
        List<Candidate> candidates = candidateRepository.findAllByPromiseId(promiseId);
        candidates.forEach(Candidate::unconfirm);
        confirmedCandidate.confirm();

        // 8. 약속 전체 상태도 확정 완료로 변경
        confirmedCandidate.getPromise().confirm();

        eventPublisher.publishEvent(new PromiseConfirmedEvent(
                confirmedCandidate.getPromise().getId(),
                confirmedCandidate.getPromise().getTitle(),
                confirmedCandidate.getPromise().getPromisedAt(),
                confirmedCandidate.getId(),
                confirmedCandidate.getName(),
                confirmedCandidate.getAddress()
        ));
    }

    @Override
    @Transactional
    public void revoteCandidates(Long userId, Long promiseId) {
        // 1. 인증된 사용자 자체가 유효한지 확인
        userRepository.findById(userId)
                .orElseThrow(UserNotFoundException::new);

        // 2. 해당 사용자가 이 약속의 참여자인지 확인
        PromiseMember promiseMember = promiseMemberRepository.findByUserIdAndPromiseId(userId, promiseId)
                .orElseThrow(PromiseMemberAccessDeniedException::new);

        // 3. 재투표 시작도 방장만 가능
        if (promiseMember.getRole() != Role.HOST) {
            throw new PromiseMemberHostOnlyException();
        }

        // 4. 현재 투표 대상 후보들과 투표 내역을 조회
        List<Candidate> activeCandidates = candidateRepository.findAllByPromiseIdAndIsActiveTrue(promiseId);
        List<CandidateVote> candidateVotes = candidateVoteRepository.findAllByPromiseId(promiseId);

        Map<Long, Long> voteCountByCandidateId = candidateVotes.stream()
                .collect(Collectors.groupingBy(vote -> vote.getCandidate().getId(), Collectors.counting()));

        long maxVoteCount = activeCandidates.stream()
                .mapToLong(candidate -> voteCountByCandidateId.getOrDefault(candidate.getId(), 0L))
                .max()
                .orElse(0L);

        // 5. 최다 득표 동점 후보가 2개 이상일 때만 재투표 가능
        List<Candidate> revoteCandidates = activeCandidates.stream()
                .filter(candidate -> voteCountByCandidateId.getOrDefault(candidate.getId(), 0L) == maxVoteCount)
                .toList();

        if (revoteCandidates.size() < 2) {
            throw new PromiseRevoteNotAvailableException();
        }

        Set<Long> revoteCandidateIds = revoteCandidates.stream()
                .map(Candidate::getId)
                .collect(Collectors.toSet());

        // 6. 동점 후보만 다시 활성화하고, 나머지 후보는 재투표 대상에서 제외
        List<Candidate> allCandidates = candidateRepository.findAllByPromiseId(promiseId);
        allCandidates.forEach(candidate -> {
            candidate.unconfirm();
            if (revoteCandidateIds.contains(candidate.getId())) {
                candidate.activate();
            } else {
                candidate.deactivate();
            }
        });

        // 7. 기존 투표를 비우고, 단일 투표 + 12시간 뒤 종료로 재설정
        candidateVoteRepository.deleteAllByPromiseId(promiseId);
        promiseMember.getPromise().startRevote(LocalDateTime.now().plusHours(12));
    }
}
