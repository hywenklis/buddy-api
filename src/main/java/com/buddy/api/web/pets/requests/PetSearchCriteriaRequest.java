package com.buddy.api.web.pets.requests;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDate;
import java.util.UUID;
import org.springframework.lang.Nullable;

@Schema(description = "Search criteria for filtering pets")
public record PetSearchCriteriaRequest(
        @Schema(description = "Pet ID", example = "123e4567-e89b-12d3-a456-426614174000")
        @Nullable UUID id,

        @Schema(description = "Shelter ID", example = "123e4567-e89b-12d3-a456-426614174001")
        @Nullable UUID shelterId,

        @Schema(description = "Name of the pet", example = "Buddy")
        @Nullable String name,

        @Schema(description = "Species of the pet", example = "Cão")
        @Nullable String species,

        @Schema(description = "Gender of the pet", example = "Macho")
        @Nullable String gender,

        @Schema(description = "Age range of the pet", example = "1-2 anos")
        @Nullable String ageRange,

        @Schema(description = "Birth date of the pet", example = "2021-01-01")
        @Nullable LocalDate birthDate,

        @Schema(description = "Location of the pet", example = "New York")
        @Nullable String location,

        @Schema(description = "Weight range of the pet", example = "10-20 kg")
        @Nullable String weightRange,

        @Schema(description = "Description of the pet", example = "A friendly dog")
        @Nullable String description
) {
}
