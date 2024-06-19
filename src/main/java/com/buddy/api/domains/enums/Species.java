package com.buddy.api.domains.enums;

public enum Species {
    CÃO("Cão"),
    GATO("Gato"),
    REPTIL("Réptil"),
    PEIXE("Peixe");

    private final String description;

    Species(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    public static Species valueOfDescription(String description) {
        for (Species species : values()) {
            if (species.description.equals(description)) {
                return species;
            }
        }
        throw new IllegalArgumentException("Unknown species: " + description);
    }
}
