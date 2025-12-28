package com.buddy.api.commons.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum Gender {
    MACHO("Macho"),
    FEMEA("Fêmea");

    private final String description;

    public static Gender valueOfDescription(final String description) {
        for (Gender gender : values()) {
            if (gender.description.equalsIgnoreCase(description) || gender.name().equalsIgnoreCase(description)) {
                return gender;
            }
        }
        return null;
    }
}
