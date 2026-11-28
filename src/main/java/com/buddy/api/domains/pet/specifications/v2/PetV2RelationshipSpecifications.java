package com.buddy.api.domains.pet.specifications.v2;

import com.buddy.api.domains.pet.entities.PetV2Entity;
import java.util.UUID;
import org.springframework.data.jpa.domain.Specification;

public final class PetV2RelationshipSpecifications {

    private static final String FIELD_GUARDIAN_PROFILE = "guardianProfile";
    private static final String FIELD_PROFILE_ID = "profileId";

    private PetV2RelationshipSpecifications() {
        throw new UnsupportedOperationException("Utility class");
    }

    public static Specification<PetV2Entity> hasGuardianProfileId(final UUID guardianProfileId) {
        return (root, query, cb) ->
            guardianProfileId == null
                ? cb.conjunction()
                : cb.equal(
                    root.get(FIELD_GUARDIAN_PROFILE).get(FIELD_PROFILE_ID),
                    guardianProfileId
                );
    }
}
