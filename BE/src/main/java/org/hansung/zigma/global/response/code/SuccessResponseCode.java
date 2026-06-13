package org.hansung.zigma.global.response.code;

import static org.hansung.zigma.global.constant.StaticValue.*;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum SuccessResponseCode implements BaseResponseCode {
    SUCCESS_OK("GLOBAL_200", OK, "호출에 성공했습니다."),
    SUCCESS_CREATED("GLOBAL_201", CREATED, "리소스가 성공적으로 생성되었습니다."),
    SUCCESS_NO_CONTENT("GLOBAL_204", NO_CONTENT, "요청은 성공했으나 응답할 콘텐츠가 없습니다.");

    private final String code;
    private final int httpStatus;
    private final String message;
}
