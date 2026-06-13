package org.hansung.zigma.global.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.JwtParser;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.hansung.zigma.global.jwt.exception.TokenInvalidException;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
@RequiredArgsConstructor
public class JwtTokenUtil {

    private final JwtParser parser;

    // JWT를 서명 검증과 함께 파싱하여 Claims를 반환
    public Claims parseClaims(String token) {
        return parser.parseSignedClaims(token).getPayload();
        // ExpiredJwtException : 토큰 만료                             ──                            그룹 1
        // UnsupportedJwtException : 서명 없는 토큰 등 지원 안되는 형식   ┐
        // MalformedJwtException : JWT 구조 자체가 깨져 있을 때          ├── JwtException 하위 클래스 → 그룹 2
        // SignatureException : 서명 검증 실패 (위변조)                  ┘
        // IllegalArgumentException : 토큰이 null 또는 빈 문자열         ──                           그룹 2
    }

    // 토큰의 subject(userId) 추출
    public String getUserId(String token) {
        return parseClaims(token).getSubject();
    }

    // 토큰 유효성(서명, 만료, 형식)을 검사 (예외를 던짐)
    public void validateTokenThrows(String token) {
        try {
            parseClaims(token); // 예외 발생

        } catch (ExpiredJwtException e) { // 토큰 만료
            throw e;
        } catch (JwtException | IllegalArgumentException e) {
            throw new TokenInvalidException();
        }
    }

    // Authorization 헤더에서 Bearer 토큰 문자열을 추출
    public String resolveToken(HttpServletRequest request) {
        String header = request.getHeader("Authorization");
        if (!StringUtils.hasText(header) || !header.startsWith("Bearer ")) {
            return null;
        }
        return header.substring(7);
    }
}
