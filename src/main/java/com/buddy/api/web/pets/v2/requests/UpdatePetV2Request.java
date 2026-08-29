package com.buddy.api.web.pets.v2.requests;

import com.buddy.api.domains.pet.enums.PetGender;
import com.buddy.api.domains.pet.enums.PetSpecies;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import lombok.Builder;

@Builder
@Schema(name = "UpdatePetV2Request", description = "Payload for updating an existing pet (V2)")
public record UpdatePetV2Request(
    @Size(min = 2, max = 100, message = "Pet name must have between 2 and 100 characters")
    @Schema(example = "Thor Updated", description = "Name of the pet")
    String name,

    @Schema(example = "DOG", description = "CAT, DOG, BIRD, REPTILE, FISH")
    PetSpecies species,

    @Schema(example = "MALE", description = "MALE, FEMALE, UNDEFINED")
    PetGender gender,

    @Min(value = 0, message = "Approximate age cannot be negative")
    @Max(value = 50, message = "Approximate age must be at most 50")
    @Schema(example = "3", description = "Estimated age in years")
    Integer approximateAge,

    @Positive(message = "Size must be greater than zero")
    @Schema(example = "48.00", description = "Size in centimeters")
    BigDecimal size,

    @Positive(message = "Weight must be greater than zero")
    @Schema(example = "15.00", description = "Weight in kilograms")
    BigDecimal weight,

    @Schema(example = "true", description = "Indicates whether the pet is neutered")
    Boolean isNeutered,

    @Schema(example = "false", description = "Indicates whether the pet is available for adoption")
    Boolean isForAdoption,

    @Size(max = 1000, message = "Description must have at most 1000 characters")
    @Schema(example = "Updated description.", description = "Description of the pet")
    String description
) {
}
