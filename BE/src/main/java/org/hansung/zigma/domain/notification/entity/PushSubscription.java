package org.hansung.zigma.domain.notification.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hansung.zigma.domain.user.entity.User;
import org.hansung.zigma.global.entity.BaseEntity;

import java.time.LocalDateTime;

@Entity
@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(
        name = "push_subscriptions",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_push_subscription_endpoint_hash",
                        columnNames = "endpoint_hash"
                )
        }
)
public class PushSubscription extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String endpoint;

    @Column(name = "endpoint_hash", nullable = false, length = 64)
    private String endpointHash;

    @Column(name = "p256dh", nullable = false, length = 255)
    private String p256dh;

    @Column(name = "auth", nullable = false, length = 255)
    private String auth;

    @Column(name = "user_agent", length = 512)
    private String userAgent;

    @Builder.Default
    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    @Column(name = "last_used_at")
    private LocalDateTime lastUsedAt;

    @Column(name = "expired_at")
    private LocalDateTime expiredAt;

    // ------------------------------ 메서드 ------------------------------
    public static PushSubscription create(
            User user,
            String endpoint,
            String endpointHash,
            String p256dh,
            String auth,
            String userAgent
    ) {
        return PushSubscription.builder()
                .user(user)
                .endpoint(endpoint)
                .endpointHash(endpointHash)
                .p256dh(p256dh)
                .auth(auth)
                .userAgent(userAgent)
                .isActive(true)
                .build();
    }

    public void renew(User user, String p256dh, String auth, String userAgent) {
        this.user = user;
        this.p256dh = p256dh;
        this.auth = auth;
        this.userAgent = userAgent;
        this.isActive = true;
        this.expiredAt = null;
    }

    public void markUsed(LocalDateTime usedAt) {
        this.lastUsedAt = usedAt;
    }

    public void expire(LocalDateTime expiredAt) {
        this.isActive = false;
        this.expiredAt = expiredAt;
    }
}
