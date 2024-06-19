package com.buddy.api.domains.enums;

public enum Gender {
    MACHO("Macho"),
    FEMEA("Fêmea");

    private final String description;

    Gender(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    public static Gender valueOfDescription(String description) {
        for (Gender gender : values()) {
            if (gender.description.equals(description)) {
                return gender;
            }
        }
        throw new IllegalArgumentException("Unknown gender: " + description);
    }
}
