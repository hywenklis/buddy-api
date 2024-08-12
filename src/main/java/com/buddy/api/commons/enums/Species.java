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

    public static Species valueOfDescription(String description) {
        for (Species species : values()) {
            if (species.description.equals(description)) {
                return species;
            }
        }
        throw new IllegalArgumentException("Unknown species: " + description);
    }
}
