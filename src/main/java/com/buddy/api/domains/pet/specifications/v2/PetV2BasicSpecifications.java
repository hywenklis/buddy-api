package com.buddy.api.domains.pet.specifications.v2;

import com.buddy.api.domains.pet.entities.PetV2Entity;
import com.buddy.api.domains.pet.enums.PetGender;
import com.buddy.api.domains.pet.enums.PetSpecies;
import org.springframework.data.jpa.domain.Specification;

public final class PetV2BasicSpecifications {

    private static final String FIELD_SPECIES = "species";
    private static final String FIELD_GENDER = "gender";
    private static final String FIELD_IS_NEUTERED = "isNeutered";
    private static final String FIELD_IS_FOR_ADOPTION = "isForAdoption";

    private PetV2BasicSpecifications() {
        throw new UnsupportedOperationException("Utility class");
    }

    public static Specification<PetV2Entity> hasSpecies(final PetSpecies species) {
        return (root, query, cb) ->
            species == null ? cb.conjunction() : cb.equal(root.get(FIELD_SPECIES), species);
    }

    public static Specification<PetV2Entity> hasGender(final PetGender gender) {
        return (root, query, cb) ->
            gender == null ? cb.conjunction() : cb.equal(root.get(FIELD_GENDER), gender);
    }

    public static Specification<PetV2Entity> isNeutered(final Boolean isNeutered) {
        return (root, query, cb) ->
            isNeutered == null
                ? cb.conjunction()
                : cb.equal(root.get(FIELD_IS_NEUTERED), isNeutered);
    }

    public static Specification<PetV2Entity> isForAdoption(final Boolean isForAdoption) {
        return (root, query, cb) ->
            isForAdoption == null
                ? cb.conjunction()
                : cb.equal(root.get(FIELD_IS_FOR_ADOPTION), isForAdoption);
    }
}
