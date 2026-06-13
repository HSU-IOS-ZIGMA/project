package org.hansung.zigma.domain.promise.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hansung.zigma.domain.promise.web.dto.PromiseCreateReq;
import org.hansung.zigma.global.entity.BaseEntity;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "promises")
public class Promise extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title; // 약속명

    @Column(name = "promised_at", nullable = false)
    private LocalDateTime promisedAt; // 약속 날짜 및 시간

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Category category; // 약속 주제

    @Builder.Default
    @Column(name = "is_multiple_voting", nullable = false)
    private Boolean isMultipleVoting = false; // 장소 복수 투표 여부

    @Column(name = "end_at", nullable = false)
    private LocalDateTime endAt; // 투표 종료 시간

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PromiseStatus status; // 약속 상태

    @Column(name = "invite_code", unique = true)
    private String inviteCode; // 초대 링크용 UUID

    @OneToMany(mappedBy = "promise", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<PromiseMember> promiseMembers = new ArrayList<>();

    @OneToMany(mappedBy = "promise", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<CandidateVote> candidateVotes = new ArrayList<>();

    // ------------------------------ 메서드 ------------------------------
    public static Promise toEntity(PromiseCreateReq req) {
        // endsAt이 null이면 약속 시간(promisedAt)의 1시간 전으로 설정
        LocalDateTime finalEndAt = (req.getEndAt() != null)
                ? req.getEndAt()
                : req.getPromisedAt().minusHours(1);

        return Promise.builder()
                .title(req.getTitle())
                .promisedAt(req.getPromisedAt())
                .category(Category.from(req.getCategory()))
                .endAt(finalEndAt)
                .isMultipleVoting(req.getIsMultipleVoting() != null ? req.getIsMultipleVoting() : false)
                .status(PromiseStatus.PENDING)
                .build();
    }

    // 양방향 연관관계 편의 메서드
    public void setPromiseMember(PromiseMember promiseMember) {
        this.promiseMembers.add(promiseMember);
        promiseMember.setPromise(this);
    }

    public void setCandidateVote(CandidateVote candidateVote) {
        this.candidateVotes.add(candidateVote);
        candidateVote.setPromise(this);
    }

    public void confirm() {
        this.status = PromiseStatus.CONFIRMED;
    }

    public void proceed() {
        if (this.status == PromiseStatus.PENDING) {
            this.status = PromiseStatus.PROCEEDING;
        }
    }

    public void pend() {
        if (this.status == PromiseStatus.PROCEEDING) {
            this.status = PromiseStatus.PENDING;
        }
    }

    public void startRevote(LocalDateTime endAt) {
        this.isMultipleVoting = false;
        this.endAt = endAt;
        this.status = PromiseStatus.PROCEEDING;
    }

    public String issueInviteCode() {
        if (this.inviteCode == null || this.inviteCode.isBlank()) {
            this.inviteCode = UUID.randomUUID().toString();
        }
        return this.inviteCode;
    }
}
