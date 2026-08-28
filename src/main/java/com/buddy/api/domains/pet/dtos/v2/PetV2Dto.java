package com.buddy.api.domains.pet.dtos.v2;

import com.buddy.api.domains.pet.enums.PetGender;
import com.buddy.api.domains.pet.enums.PetSpecies;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import lombok.Builder;

@Builder
public record PetV2Dto(
    UUID id,
    UUID guardianProfileId,
    String name,
    PetSpecies species,
    PetGender gender,
    Integer approximateAge,
    LocalDate ageReportDate,
    BigDecimal size,
    BigDecimal weight,
    Boolean isNeutered,
    Boolean isForAdoption,
    String description,
    String coverImageUrl,
    List<PetV2ImageDto> images,
    LocalDateTime creationDate,
    LocalDateTime updatedDate
) {
    public PetV2Dto {
        images = images == null ? List.of() : List.copyOf(images);
    }

    @Override
    public List<PetV2ImageDto> images() {
        return List.copyOf(this.images);
    }
}
