package com.buddy.api.web.pets.v2.requests;

import com.buddy.api.domains.pet.enums.PetGender;
import com.buddy.api.domains.pet.enums.PetSpecies;
import io.swagger.v3.oas.annotations.Parameter;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.PositiveOrZero;
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
    @PositiveOrZero(message = "minSize must be greater than or equal to 0")
    BigDecimal minSize,

    @Parameter(description = "Maximum size in centimeters")
    @PositiveOrZero(message = "maxSize must be greater than or equal to 0")
    BigDecimal maxSize,

    @Parameter(description = "Minimum weight in kilograms")
    @PositiveOrZero(message = "minWeight must be greater than or equal to 0")
    BigDecimal minWeight,

    @Parameter(description = "Maximum weight in kilograms")
    @PositiveOrZero(message = "maxWeight must be greater than or equal to 0")
    BigDecimal maxWeight,

    @Parameter(description = "Minimum approximate age in years")
    @Min(value = 0, message = "minAge must be greater than or equal to 0")
    Integer minAge,

    @Parameter(description = "Maximum approximate age in years")
    @Min(value = 0, message = "maxAge must be greater than or equal to 0")
    Integer maxAge
) {

    @AssertTrue(message = "minSize cannot be greater than maxSize")
    public boolean isSizeRangeValid() {
        return minSize == null || maxSize == null || minSize.compareTo(maxSize) <= 0;
    }

    @AssertTrue(message = "minWeight cannot be greater than maxWeight")
    public boolean isWeightRangeValid() {
        return minWeight == null || maxWeight == null || minWeight.compareTo(maxWeight) <= 0;
    }

    @AssertTrue(message = "minAge cannot be greater than maxAge")
    public boolean isAgeRangeValid() {
        return minAge == null || maxAge == null || minAge <= maxAge;
    }
}
