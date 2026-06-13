package org.hansung.zigma.domain.promise.entity;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum PromiseStatus {
    PENDING("장소 미정"),     // 회색 배경
    PROCEEDING("진행 중"),   // 주황색 배경
    CONFIRMED("확정 완료");   // 보라색 배경

    private final String value;

    @JsonValue
    public String getValue() {
        return value;
    }

    @JsonCreator
    public static PromiseStatus from(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }

        for (PromiseStatus status : PromiseStatus.values()) {
            if (status.getValue().equals(value)) {
                return status;
            }
        }
        return null;
    }
}