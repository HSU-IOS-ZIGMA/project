package org.hansung.zigma.domain.promise.web.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;

@Getter
public class CandidateConfirmReq {

    @NotNull(message = "확정할 후보지 ID는 필수 입력 항목입니다.")
    private Long candidateId;
}
