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
@Table(name = "notifications")
public class Notification extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private NotificationType type;

    @Column(nullable = false, length = 100)
    private String title;

    @Column(nullable = false, length = 500)
    private String body;

    @Enumerated(EnumType.STRING)
    @Column(name = "target_type", nullable = false, length = 50)
    private NotificationTargetType targetType;

    @Column(name = "target_id", nullable = false)
    private Long targetId;

    @Column(name = "link_url", length = 255)
    private String linkUrl;

    @Column(name = "sent_at")
    private LocalDateTime sentAt;

    @Column(name = "read_at")
    private LocalDateTime readAt;

    // ------------------------------ 메서드 ------------------------------
    public static Notification create(
            User user,
            NotificationType type,
            String title,
            String body,
            NotificationTargetType targetType,
            Long targetId,
            String linkUrl
    ) {
        return Notification.builder()
                .user(user)
                .type(type)
                .title(title)
                .body(body)
                .targetType(targetType)
                .targetId(targetId)
                .linkUrl(linkUrl)
                .build();
    }

    public void markSent(LocalDateTime sentAt) {
        this.sentAt = sentAt;
    }

    public void markRead(LocalDateTime readAt) {
        this.readAt = readAt;
    }
}
