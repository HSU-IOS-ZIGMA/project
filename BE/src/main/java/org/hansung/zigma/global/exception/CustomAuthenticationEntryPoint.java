package org.hansung.zigma.global.exception;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.hansung.zigma.global.response.ErrorResponse;
import org.hansung.zigma.global.response.code.ErrorResponseCode;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class CustomAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private final ObjectMapper objectMapper; // JSON 직렬화 도구

    @Override
    public void commence(
            HttpServletRequest request,
            HttpServletResponse response,
            AuthenticationException authException
    ) throws IOException, ServletException {
        // 필터에서 넣어둔 예외 정보 꺼내기
        String exception = (String) request.getAttribute("exception");
        ErrorResponseCode errorCode;

        // 예외 종류에 따라 에러 코드 결정
        if ("EXPIRED_TOKEN".equals(exception)) {
            errorCode = ErrorResponseCode.EXPIRED_TOKEN; // 만료된 토큰
        } else if ("INVALID_TOKEN".equals(exception)) {
            errorCode = ErrorResponseCode.INVALID_TOKEN; // 잘못된 토큰
        } else {
            errorCode = ErrorResponseCode.UNAUTHORIZED_REQUEST; // 그 외 인증 실패
        }

        setResponse(response, errorCode);
    }

    private void setResponse(HttpServletResponse response, ErrorResponseCode errorCode) throws IOException {
        response.setStatus(errorCode.getHttpStatus());
        response.setContentType("application/json;charset=UTF-8");
        ErrorResponse<?> body = ErrorResponse.from(errorCode);
        response.getWriter().write(objectMapper.writeValueAsString(body));
    }
}