package org.hansung.zigma.domain.promise.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hansung.zigma.domain.user.entity.User;
import org.hansung.zigma.global.entity.BaseEntity;

@Entity
@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(
        name = "promise_members",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_promise_member_user_promise",
                        columnNames = {"user_id", "promise_id"}
                )
        }
)
public class PromiseMember extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "promise_id")
    @Setter(AccessLevel.PACKAGE)
    private Promise promise;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;

    // ------------------------------ 메서드 ------------------------------
    public static PromiseMember createMember(User user, Promise promise, Role role) {
        return PromiseMember.builder()
                .user(user)
                .promise(promise)
                .role(role)
                .build();
    }
}
