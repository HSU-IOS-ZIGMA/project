package org.hansung.zigma.domain.promise.entity;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.hansung.zigma.domain.promise.exception.PromiseCategoryInvalidException;

@Getter
@RequiredArgsConstructor
public enum Category {
    MEAL("식사"),
    CAFE("카페"),
    MOVIE("영화"),
    ACTIVITY("액티비티"),
    STUDY("스터디"),
    PARTY("파티");

    private final String value;

    @JsonValue
    public String getValue() {
        return value;
    }

    public static Category from(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }

        for (Category category : Category.values()) {
            if (category.getValue().equals(value)) {
                return category;
            }
        }
        throw new PromiseCategoryInvalidException();
    }
}