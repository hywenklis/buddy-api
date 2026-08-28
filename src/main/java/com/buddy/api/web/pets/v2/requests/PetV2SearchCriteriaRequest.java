package com.buddy.api.web.pets.v2.requests;

import com.buddy.api.domains.pet.enums.PetGender;
import com.buddy.api.domains.pet.enums.PetSpecies;
import io.swagger.v3.oas.annotations.Parameter;
import java.math.BigDecimal;
import java.util.UUID;
import lombok.Builder;

@Builder
public record PetV2SearchCriteriaRequest(
    @Parameter(description = "Partial name search (case-insensitive)")
    String name,

    @Parameter(description = "Pet species: CAT, DOG, BIRD, REPTILE, FISH")
    PetSpecies species,

    @Parameter(description = "Pet gender: MALE, FEMALE, UNDEFINED")
    PetGender gender,

    @Parameter(description = "Filter by neutered status")
    Boolean isNeutered,

    @Parameter(description = "Filter by adoption availability (default: true)")
    Boolean isForAdoption,

    @Parameter(description = "Filter by guardian profile UUID")
    UUID guardianProfileId,

    @Parameter(description = "Minimum size in centimeters")
    BigDecimal minSize,

    @Parameter(description = "Maximum size in centimeters")
    BigDecimal maxSize,

    @Parameter(description = "Minimum weight in kilograms")
    BigDecimal minWeight,

    @Parameter(description = "Maximum weight in kilograms")
    BigDecimal maxWeight,

    @Parameter(description = "Minimum approximate age in years")
    Integer minAge,

    @Parameter(description = "Maximum approximate age in years")
    Integer maxAge
) {
}
