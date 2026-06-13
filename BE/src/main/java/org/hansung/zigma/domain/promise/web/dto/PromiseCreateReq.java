package org.hansung.zigma.domain.promise.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class PromiseCreateReq {
    @NotBlank(message = "약속명은 필수 입력 항목입니다.")
    private String title;

    @NotNull(message = "약속 날짜와 시간은 필수 입력 항목입니다.")
    private LocalDateTime promisedAt;

    @NotNull(message = "약속 주제는 필수 입력 항목입니다.")
    private String category;

    private LocalDateTime endAt;

    private Boolean isMultipleVoting;
}
