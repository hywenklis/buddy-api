package com.buddy.api.domains.pet.specifications.v2;

import com.buddy.api.domains.pet.dtos.v2.PetV2SearchCriteriaDto;
import com.buddy.api.domains.pet.entities.PetV2Entity;
import com.buddy.api.domains.pet.enums.PetGender;
import com.buddy.api.domains.pet.enums.PetSpecies;
import java.math.BigDecimal;
import java.util.Locale;
import java.util.UUID;
import org.springframework.data.jpa.domain.Specification;

public final class PetV2Specifications {

    private static final String FIELD_SPECIES = "species";
    private static final String FIELD_GENDER = "gender";
    private static final String FIELD_NAME = "name";
    private static final String FIELD_SIZE = "size";
    private static final String FIELD_WEIGHT = "weight";
    private static final String FIELD_IS_NEUTERED = "isNeutered";
    private static final String FIELD_IS_FOR_ADOPTION = "isForAdoption";
    private static final String FIELD_APPROXIMATE_AGE = "approximateAge";
    private static final String FIELD_GUARDIAN_PROFILE = "guardianProfile";
    private static final String FIELD_PROFILE_ID = "profileId";

    private PetV2Specifications() {
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

    public static Specification<PetV2Entity> nameContains(final String name) {
        return (root, query, cb) -> {
            if (name == null || name.isBlank()) {
                return cb.conjunction();
            }
            final var pattern = "%" + name.trim().toLowerCase(Locale.ROOT) + "%";
            return cb.like(cb.lower(root.get(FIELD_NAME)), pattern);
        };
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

    public static Specification<PetV2Entity> hasGuardianProfileId(final UUID guardianProfileId) {
        return (root, query, cb) ->
            guardianProfileId == null
                ? cb.conjunction()
                : cb.equal(
                    root.get(FIELD_GUARDIAN_PROFILE).get(FIELD_PROFILE_ID),
                    guardianProfileId
                );
    }

    public static Specification<PetV2Entity> sizeBetween(final BigDecimal min,
                                                         final BigDecimal max) {
        return (root, query, cb) -> {
            if (min == null && max == null) {
                return cb.conjunction();
            }
            if (min != null && max != null) {
                return cb.between(root.get(FIELD_SIZE), min, max);
            }
            if (min != null) {
                return cb.greaterThanOrEqualTo(root.get(FIELD_SIZE), min);
            }
            return cb.lessThanOrEqualTo(root.get(FIELD_SIZE), max);
        };
    }

    public static Specification<PetV2Entity> weightBetween(final BigDecimal min,
                                                           final BigDecimal max) {
        return (root, query, cb) -> {
            if (min == null && max == null) {
                return cb.conjunction();
            }
            if (min != null && max != null) {
                return cb.between(root.get(FIELD_WEIGHT), min, max);
            }
            if (min != null) {
                return cb.greaterThanOrEqualTo(root.get(FIELD_WEIGHT), min);
            }
            return cb.lessThanOrEqualTo(root.get(FIELD_WEIGHT), max);
        };
    }

    public static Specification<PetV2Entity> ageBetween(final Integer min,
                                                        final Integer max) {
        return (root, query, cb) -> {
            if (min == null && max == null) {
                return cb.conjunction();
            }
            if (min != null && max != null) {
                return cb.between(root.get(FIELD_APPROXIMATE_AGE), min, max);
            }
            if (min != null) {
                return cb.greaterThanOrEqualTo(root.get(FIELD_APPROXIMATE_AGE), min);
            }
            return cb.lessThanOrEqualTo(root.get(FIELD_APPROXIMATE_AGE), max);
        };
    }

    public static Specification<PetV2Entity> withCriteria(final PetV2SearchCriteriaDto criteria) {
        if (criteria == null) {
            return (root, query, cb) -> cb.conjunction();
        }
        return Specification.where(hasSpecies(criteria.species()))
            .and(hasGender(criteria.gender()))
            .and(nameContains(criteria.name()))
            .and(isNeutered(criteria.isNeutered()))
            .and(isForAdoption(criteria.isForAdoption()))
            .and(hasGuardianProfileId(criteria.guardianProfileId()))
            .and(sizeBetween(criteria.minSize(), criteria.maxSize()))
            .and(weightBetween(criteria.minWeight(), criteria.maxWeight()))
            .and(ageBetween(criteria.minAge(), criteria.maxAge()));
    }
}
