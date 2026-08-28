package com.buddy.api.domains.pet.specifications.v2;

import static com.buddy.api.domains.pet.specifications.v2.PetV2BasicSpecifications.hasGender;
import static com.buddy.api.domains.pet.specifications.v2.PetV2BasicSpecifications.hasSpecies;
import static com.buddy.api.domains.pet.specifications.v2.PetV2BasicSpecifications.isForAdoption;
import static com.buddy.api.domains.pet.specifications.v2.PetV2BasicSpecifications.isNeutered;
import static com.buddy.api.domains.pet.specifications.v2.PetV2MetricSpecifications.ageBetween;
import static com.buddy.api.domains.pet.specifications.v2.PetV2MetricSpecifications.sizeBetween;
import static com.buddy.api.domains.pet.specifications.v2.PetV2MetricSpecifications.weightBetween;
import static com.buddy.api.domains.pet.specifications.v2.PetV2TextSpecifications.nameContains;

import com.buddy.api.domains.pet.dtos.v2.PetV2SearchCriteriaDto;
import com.buddy.api.domains.pet.entities.PetV2Entity;
import org.springframework.data.jpa.domain.Specification;

public final class PetV2Specifications {

    private PetV2Specifications() {
        throw new UnsupportedOperationException("Utility class");
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
            .and(PetV2RelationshipSpecifications
                .hasGuardianProfileId(criteria.guardianProfileId()))
            .and(sizeBetween(criteria.minSize(), criteria.maxSize()))
            .and(weightBetween(criteria.minWeight(), criteria.maxWeight()))
            .and(ageBetween(criteria.minAge(), criteria.maxAge()));
    }
}
