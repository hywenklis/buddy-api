package com.buddy.api.commons.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum Gender {
    MACHO("Macho"),
    FEMEA("Fêmea");

    private final String description;

    public static Gender valueOfDescription(String description) {
        for (Gender gender : values()) {
            if (gender.description.equals(description)) {
                return gender;
            }
        }
        throw new IllegalArgumentException("Unknown gender: " + description);
    }
}
