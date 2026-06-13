package org.hansung.zigma.domain.comment.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;

@Getter
public class CommentCreateReq {

    @NotBlank(message = "코멘트 내용은 필수 입력 항목입니다.")
    private String content;

    @NotNull(message = "위도(latitude)는 필수 입력 항목입니다.")
    private Double latitude;

    @NotNull(message = "경도(longitude)는 필수 입력 항목입니다.")
    private Double longitude;
}
