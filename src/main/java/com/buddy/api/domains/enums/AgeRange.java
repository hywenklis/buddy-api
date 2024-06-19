package com.buddy.api.domains.enums;

public enum AgeRange {
    ZERO_TO_ONE("0-1 ano", 0, 1),
    ONE_TO_TWO("1-2 anos", 1, 2),
    TWO_TO_THREE("2-3 anos", 2, 3),
    THREE_TO_FIVE("3-5 anos", 3, 5),
    FIVE_TO_TEN("5-10 anos", 5, 10),
    TEN_PLUS("10+ anos", 10, Integer.MAX_VALUE);

    private final String description;
    private final int min;
    private final int max;

    AgeRange(String description, int min, int max) {
        this.description = description;
        this.min = min;
        this.max = max;
    }

    public String getDescription() {
        return description;
    }

    public int getMin() {
        return min;
    }

    public int getMax() {
        return max;
    }

    public static AgeRange fromDescription(String description) {
        for (AgeRange range : values()) {
            if (range.description.equals(description)) {
                return range;
            }
        }
        throw new IllegalArgumentException("Unknown age range: " + description);
    }
}


