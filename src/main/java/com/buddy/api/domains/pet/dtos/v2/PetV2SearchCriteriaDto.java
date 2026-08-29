package com.buddy.api.domains.pet.dtos.v2;

import com.buddy.api.domains.pet.enums.PetGender;
import com.buddy.api.domains.pet.enums.PetSpecies;
import java.math.BigDecimal;
import java.util.UUID;
import lombok.Builder;

@Builder
public record PetV2SearchCriteriaDto(
    String name,
    PetSpecies species,
    PetGender gender,
    Boolean isNeutered,
    Boolean isForAdoption,
    UUID guardianProfileId,
    BigDecimal minSize,
    BigDecimal maxSize,
    BigDecimal minWeight,
    BigDecimal maxWeight,
    Integer minAge,
    Integer maxAge
) {
}
