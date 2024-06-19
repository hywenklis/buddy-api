package com.buddy.api.domains.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum WeightRange {
    ZERO_TO_FIVE("0-5 kg", 0.0, 5.0),
    FIVE_TO_TEN("5-10 kg", 5.0, 10.0),
    TEN_TO_TWENTY("10-20 kg", 10.0, 20.0),
    TWENTY_TO_THIRTY("20-30 kg", 20.0, 30.0),
    THIRTY_PLUS("30+ kg", 30.0, Double.MAX_VALUE);

    private final String description;
    private final double min;
    private final double max;

    public static WeightRange fromDescription(String description) {
        for (WeightRange range : values()) {
            if (range.description.equals(description)) {
                return range;
            }
        }
        throw new IllegalArgumentException("Unknown weight range: " + description);
    }
}
