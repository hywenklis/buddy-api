package com.buddy.api.web.pets.v2.responses;

import com.buddy.api.domains.pet.enums.PetGender;
import com.buddy.api.domains.pet.enums.PetSpecies;
import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import lombok.Builder;

@Builder
@Schema(name = "PetV2Response", description = "Detailed pet response (V2)")
public record PetV2Response(
    @Schema(example = "9b1deb4d-3b7d-4bad-9bdd-2b0d7b3dcb6d")
    UUID id,

    @Schema(example = "550e8400-e29b-41d4-a716-446655440000")
    UUID guardianProfileId,

    @Schema(example = "Thor")
    String name,

    @Schema(example = "DOG")
    PetSpecies species,

    @Schema(example = "MALE")
    PetGender gender,

    @Schema(example = "2")
    Integer approximateAge,

    @Schema(example = "2026-08-27")
    LocalDate ageReportDate,

    @Schema(example = "45.50")
    BigDecimal size,

    @Schema(example = "14.20")
    BigDecimal weight,

    @Schema(example = "true")
    Boolean isNeutered,

    @Schema(example = "true")
    Boolean isForAdoption,

    @Schema(example = "Friendly and vaccinated dog.")
    String description,

    @Schema(example = "https://cdn.buddy.com/images/pets/thor-cover.jpg")
    String coverImageUrl,

    List<PetV2ImageResponse> images,

    @Schema(example = "2026-08-27T19:00:00")
    LocalDateTime creationDate,

    @Schema(example = "2026-08-27T19:00:00")
    LocalDateTime updatedDate
) {
    public PetV2Response {
        images = images == null ? List.of() : List.copyOf(images);
    }

    @Override
    public List<PetV2ImageResponse> images() {
        return List.copyOf(this.images);
    }
}
