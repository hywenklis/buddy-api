package com.buddy.api.web.pets.v2.requests;

import com.buddy.api.domains.pet.enums.PetGender;
import com.buddy.api.domains.pet.enums.PetSpecies;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import lombok.Builder;

@Builder
@Schema(name = "CreatePetV2Request", description = "Payload for registering a new pet (V2)")
public record CreatePetV2Request(
    @NotBlank(message = "Pet name is mandatory")
    @Size(min = 2, max = 100, message = "Pet name must have between 2 and 100 characters")
    @Schema(example = "Thor", description = "Name of the pet")
    String name,

    @NotNull(message = "Pet species is mandatory")
    @Schema(example = "DOG", description = "CAT, DOG, BIRD, REPTILE, FISH")
    PetSpecies species,

    @NotNull(message = "Pet gender is mandatory")
    @Schema(example = "MALE", description = "MALE, FEMALE, UNDEFINED")
    PetGender gender,

    @Min(value = 0, message = "Approximate age cannot be negative")
    @Max(value = 50, message = "Approximate age must be at most 50")
    @Schema(example = "2", description = "Estimated age in years")
    Integer approximateAge,

    @Positive(message = "Size must be greater than zero")
    @Schema(example = "45.50", description = "Size in centimeters")
    BigDecimal size,

    @Positive(message = "Weight must be greater than zero")
    @Schema(example = "14.20", description = "Weight in kilograms")
    BigDecimal weight,

    @Schema(example = "true", description = "Indicates whether the pet is neutered")
    Boolean isNeutered,

    @Schema(example = "true", description = "Indicates whether the pet is available for adoption")
    Boolean isForAdoption,

    @Size(max = 1000, message = "Description must have at most 1000 characters")
    @Schema(example = "Friendly and vaccinated dog.", description = "Description of the pet")
    String description
) {
    public CreatePetV2Request {
        isForAdoption = isForAdoption == null ? Boolean.TRUE : isForAdoption;
    }
}
