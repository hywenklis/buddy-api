package com.buddy.api.domains.pet.specifications.v2;

import com.buddy.api.domains.pet.entities.PetV2Entity;
import java.util.Locale;
import org.springframework.data.jpa.domain.Specification;

public final class PetV2TextSpecifications {

    private static final String FIELD_NAME = "name";

    private PetV2TextSpecifications() {
        throw new UnsupportedOperationException("Utility class");
    }

    public static Specification<PetV2Entity> nameContains(final String name) {
        return (root, query, cb) -> {
            if (name == null || name.isBlank()) {
                return cb.conjunction();
            }
            final var pattern = "%" + name.trim().toLowerCase(Locale.ROOT) + "%";
            return cb.like(cb.lower(root.get(FIELD_NAME)), pattern);
        };
    }
}
