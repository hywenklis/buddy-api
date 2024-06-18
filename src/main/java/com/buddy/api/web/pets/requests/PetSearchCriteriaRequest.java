package com.buddy.api.web.pets.requests;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.annotation.Nullable;
import java.time.LocalDate;
import java.util.UUID;

@Schema(description = "Search criteria for filtering pets")
public record PetSearchCriteriaRequest(
        @Schema(description = "Pet ID", example = "123e4567-e89b-12d3-a456-426614174000")
        @Nullable UUID id,

        @Schema(description = "Shelter ID", example = "123e4567-e89b-12d3-a456-426614174001")
        @Nullable UUID shelterId,

        @Schema(description = "Name of the pet", example = "Buddy")
        @Nullable String name,

        @Schema(description = "Species of the pet", example = "Dog")
        @Nullable String specie,

        @Schema(description = "Sex of the pet", example = "Male")
        @Nullable String sex,

        @Schema(description = "Age of the pet in years", example = "2")
        @Nullable Integer age,

        @Schema(description = "Birth date of the pet", example = "2021-01-01")
        @Nullable LocalDate birthDate,

        @Schema(description = "Location of the pet", example = "New York")
        @Nullable String location,

        @Schema(description = "Weight of the pet in kilograms", example = "10.5")
        @Nullable Double weight,

        @Schema(description = "Description of the pet", example = "A friendly dog")
        @Nullable String description
) {
}
