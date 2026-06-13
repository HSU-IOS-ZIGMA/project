package org.hansung.zigma.global.jwt;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.hansung.zigma.domain.user.entity.User;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

@Getter
public class CustomUserDetails implements UserDetails, OAuth2User {

    private final User user;
    private final Map<String, Object> attributes;

    // JWT 필터 — 매 API 요청마다
    public CustomUserDetails(User user) {
        this.user = user;
        this.attributes = Map.of();
    }

    // 	OAuth2 로그인 — 소셜 로그인 시 1회
    public CustomUserDetails(User user, Map<String, Object> attributes) {
        this.user = user;
        this.attributes = attributes;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return new ArrayList<>();
    }

    @Override
    public String getPassword() {
        return "";
    }

    @Override
    public String getUsername() {
        return user.getId().toString();
    }

    @Override
    public Map<String, Object> getAttributes() { return attributes; }

    @Override
    public String getName() { return user.getId().toString(); }

    // --- 계정 상태 관련 메서드들 (특별한 로직이 없다면 모두 true 반환) ---

    @Override
    public boolean isAccountNonExpired() {
        return true; // 계정이 만료되지 않았음
    }

    @Override
    public boolean isAccountNonLocked() {
        return true; // 계정이 잠기지 않았음
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true; // 자격 증명(비밀번호)이 만료되지 않았음
    }

    @Override
    public boolean isEnabled() {
        return true; // 계정이 활성화됨
    }
}