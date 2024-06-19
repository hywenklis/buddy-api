package com.buddy.api.web.pets.requests;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import lombok.Builder;

@Builder
public record PetRequest(
        @Schema(description = "Name of the pet", example = "Buddy")
        @NotBlank(message = "Name of mandatory pet")
        String name,

        @Schema(description = "Species of the pet",
                example = "Cachorro, Gato, Pássaro, Peixe, Réptil")
        @NotBlank(message = "Specie of mandatory pet")
        String specie,

        @Schema(description = "Gender of the pet", example = "Macho, Fêmea")
        @NotBlank(message = "Gender of mandatory pet")
        String gender,

        @Schema(description = "Birth date of the pet", example = "2021-01-01")
        LocalDate birthDate,

        @Schema(description = "Location of the pet", example = "Maceió, Alagoas")
        String location,

        @NotNull(message = "Weight of mandatory pet")
        Double weight,

        @NotBlank(message = "Description of mandatory pet")
        String description,

        String avatar,
        List<PetImageRequest> images,

        @NotNull(message = "ShelterId of mandatory pet")
        UUID shelterId
) {}
