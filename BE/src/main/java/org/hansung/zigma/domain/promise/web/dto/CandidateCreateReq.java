package org.hansung.zigma.domain.promise.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;

@Getter
public class CandidateCreateReq {

    @NotBlank(message = "장소 이름은 필수 입력 항목입니다.")
    private String name;

    @NotBlank(message = "주소는 필수 입력 항목입니다.")
    private String address;

    @NotNull(message = "위도(latitude)는 필수 입력 항목입니다.")
    private Double latitude;

    @NotNull(message = "경도(longitude)는 필수 입력 항목입니다.")
    private Double longitude;

    private String category;
}
