package com.buddy.api.domains.pet.services.v2;

import com.buddy.api.domains.pet.dtos.v2.PetV2Dto;
import java.util.UUID;

public interface GetPetV2 {
    PetV2Dto findById(UUID id);
}
