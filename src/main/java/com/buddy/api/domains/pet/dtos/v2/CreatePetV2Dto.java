package com.buddy.api.domains.pet.dtos.v2;

import com.buddy.api.domains.pet.enums.PetGender;
import com.buddy.api.domains.pet.enums.PetSpecies;
import java.math.BigDecimal;
import java.util.UUID;
import lombok.Builder;

@Builder
public record CreatePetV2Dto(
    UUID guardianProfileId,
    String name,
    PetSpecies species,
    PetGender gender,
    Integer approximateAge,
    BigDecimal size,
    BigDecimal weight,
    Boolean isNeutered,
    Boolean isForAdoption,
    String description
) {
}
