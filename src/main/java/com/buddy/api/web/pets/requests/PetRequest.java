package com.buddy.api.web.pets.requests;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import lombok.Builder;

@Builder
public record PetRequest(@Schema(description = "Name of the pet", example = "Buddy")
                         @NotBlank(message = "Pet name is mandatory")
                         String name,

                         @Schema(description = "Species of the pet",
                                 example = "Cachorro, Gato, Pássaro, Peixe, Réptil")
                         @NotBlank(message = "Pet species is mandatory")
                         String specie,

                         @Schema(description = "Gender of the pet", example = "Macho, Fêmea")
                         @NotBlank(message = "Pet gender is mandatory")
                         String gender,

                         @Schema(description = "Birth date of the pet", example = "2021-01-01")
                         LocalDate birthDate,

                         @Schema(description = "Location of the pet", example = "Maceió, Alagoas")
                         String location,

                         @NotNull(message = "Pet weight is mandatory")
                         Double weight,

                         @NotBlank(message = "Pet description is mandatory")
                         String description,

                         String avatar,
                         List<PetImageRequest> images,

                         @NotNull(message = "Shelter ID is mandatory")
                         UUID shelterId
) {
}
