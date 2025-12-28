package com.buddy.api.commons.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum Species {
    CAO("Cão"),
    GATO("Gato"),
    PASSARO("Pássaro"),
    REPTIL("Réptil"),
    PEIXE("Peixe");

    private final String description;

    public static Species valueOfDescription(final String description) {
        for (Species species : values()) {
            if (species.description.equalsIgnoreCase(description) || species.name().equalsIgnoreCase(description)) {
                return species;
            }
        }
        return null;
    }
}
