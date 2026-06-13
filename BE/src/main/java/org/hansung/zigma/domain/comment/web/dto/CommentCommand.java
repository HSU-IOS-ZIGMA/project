package org.hansung.zigma.domain.comment.web.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

/**
 * [커맨드 객체] 지도 범위 기반 코멘트 조회를 위한 요청 데이터 캡슐화 클래스
 * 역할: GET 요청의 쿼리 스트링(?minLat=...&maxLat=...)을 Java 객체로 자동 바인딩
 * 특징: 컨트롤러 파라미터가 비대해지는 것을 방지하고, 유효성 검사(Validation)를 중앙화함
 */
@Getter
@Setter // GET 요청의 쿼리 파라미터 바인딩을 위해 Setter가 필요합니다.
public class CommentCommand {
    @NotNull(message = "최소 위도값이 누락되었습니다.")
    @Min(-90) @Max(90)
    private Double minLat;

    @NotNull(message = "최대 위도값이 누락되었습니다.")
    @Min(-90) @Max(90)
    private Double maxLat;

    @NotNull(message = "최소 경도값이 누락되었습니다.")
    @Min(-180) @Max(180)
    private Double minLng;

    @NotNull(message = "최대 경도값이 누락되었습니다.")
    @Min(-180) @Max(180)
    private Double maxLng;
}
