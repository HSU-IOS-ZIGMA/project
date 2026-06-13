package org.hansung.zigma.domain.user.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hansung.zigma.global.entity.BaseEntity;

@Entity
@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "users")
public class User extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "provider", nullable = false)
    private UserProvider provider;

    @Column(name = "provider_id", nullable = false)
    private String providerId;

    @Column(name = "email", nullable = false)
    private String email;

    @Column(name = "nick_name", nullable = false)
    private String nickName;

    @Column(name = "profile_image_url", nullable = true) // 사용자가 설정하지 않으면 null로 넘어옴
    private String profileImageUrl;

    @Column(name = "bio", nullable = true)
    private String bio; // 한줄 소개

    // ---------- 메서드 ----------
    public static User create(UserProvider provider, String providerId, String email, String nickName, String profileImageUrl) {
        return User.builder()
                .provider(provider)
                .providerId(providerId)
                .email(email)
                .nickName(nickName)
                .profileImageUrl(profileImageUrl)
                .build();
    }
}
