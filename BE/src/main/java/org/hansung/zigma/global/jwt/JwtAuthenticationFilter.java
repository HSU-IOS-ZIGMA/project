package org.hansung.zigma.global.jwt;

import io.jsonwebtoken.ExpiredJwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.hansung.zigma.domain.user.entity.User;
import org.hansung.zigma.global.jwt.exception.TokenInvalidException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenUtil jwtTokenUtil;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {
        if (SecurityContextHolder.getContext().getAuthentication() == null) { // 인증 정보가 없으면 토큰 검증
            // 토큰 문자열을 추출
            String token = jwtTokenUtil.resolveToken(request);

            try {
                if (token != null) {
                    jwtTokenUtil.validateTokenThrows(token); // 토큰 유효성 검사

                    Authentication authentication = getAuthentication(request, token);

                    SecurityContextHolder.getContext().setAuthentication(authentication); // 인증 정보를 컨텍스트에 저장
                }
            } catch (ExpiredJwtException e) {
                request.setAttribute("exception", "EXPIRED_TOKEN");
            } catch (TokenInvalidException e) {
                request.setAttribute("exception", "INVALID_TOKEN");
            }
        }

        filterChain.doFilter(request, response);
    }

    // 인증 정보 생성
    private Authentication getAuthentication(HttpServletRequest request, String token) {
        String userId = jwtTokenUtil.getUserId(token);

        User tempUser = User.builder()
                .id(Long.valueOf(userId))
                .build();

        CustomUserDetails userDetails = new CustomUserDetails(tempUser);

        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities()); // 인증 정보 생성

        authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request)); // 부가 정보 설정

        return authentication;
    }
}