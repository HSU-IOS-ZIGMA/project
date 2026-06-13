package org.hansung.zigma.global.response.code;

import static org.hansung.zigma.global.constant.StaticValue.*;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ErrorResponseCode implements BaseResponseCode {
    BAD_REQUEST_ERROR("GLOBAL_400_1", BAD_REQUEST, "잘못된 요청입니다."),
    INVALID_HTTP_MESSAGE_BODY("GLOBAL_400_2", BAD_REQUEST, "HTTP 요청 바디의 형식이 잘못되었습니다."),
    INVALID_HTTP_MESSAGE_PARAMETER("GLOBAL_400_3", BAD_REQUEST, "HTTP 요청 파라미터 형식이 잘못되었습니다."),
    UNAUTHORIZED_REQUEST("GLOBAL_401_1", UNAUTHORIZED, "인증이 필요합니다."),
    EXPIRED_TOKEN("GLOBAL_401_2", UNAUTHORIZED, "만료된 토큰입니다."),
    INVALID_TOKEN("GLOBAL_401_3", UNAUTHORIZED, "유효하지 않은 토큰입니다."),
    TOKEN_NOT_MATCH("GLOBAL_401_4", UNAUTHORIZED, "토큰 정보가 일치하지 않습니다."),
    ACCESS_DENIED_REQUEST("GLOBAL_403", FORBIDDEN, "해당 요청에 접근 권한이 없습니다."),
    NOT_FOUND_ENDPOINT("GLOBAL_404", NOT_FOUND, "존재하지 않는 앤드포인트입니다. 요청 URL을 확인해주세요."),
    UNSUPPORTED_HTTP_METHOD("GLOBAL_405", METHOD_NOT_ALLOWED, "지원하지 않는 HTTP 메소드입니다."),
    SERVER_ERROR("GLOBAL_500", INTERNAL_SERVER_ERROR, "서버 내부에서 알 수 없는 에러가 발생했습니다.");

    private final String code;
    private final int httpStatus;
    private final String message;
}
