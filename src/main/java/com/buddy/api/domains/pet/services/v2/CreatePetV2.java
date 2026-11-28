package com.buddy.api.domains.pet.services.v2;

import com.buddy.api.domains.pet.dtos.v2.CreatePetV2Dto;
import com.buddy.api.domains.pet.dtos.v2.PetV2Dto;

public interface CreatePetV2 {
    PetV2Dto create(CreatePetV2Dto dto);
}
