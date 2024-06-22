package com.buddy.api.domains.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum AgeRange {
    ZERO_TO_ONE("0-1 anos", 0L, 1L),
    ONE_TO_TWO("1-2 anos", 1L, 2L),
    TWO_TO_THREE("2-3 anos", 2L, 3L),
    THREE_TO_FIVE("3-5 anos", 3L, 5L),
    FIVE_TO_TEN("5-10 anos", 5L, 10L),
    TEN_PLUS("10+ anos", 10L, null);

    private final String description;
    private final Long min;
    private final Long max;

    public static AgeRange fromDescription(String description) {
        for (AgeRange range : values()) {
            if (range.description.equals(description)) {
                return range;
            }
        }
        throw new IllegalArgumentException("Unknown age range: " + description);
    }
}
