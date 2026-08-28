package com.buddy.api.domains.pet.services.v2;

import com.buddy.api.domains.pet.dtos.v2.PetV2Dto;
import com.buddy.api.domains.pet.dtos.v2.UpdatePetV2Dto;
import java.util.UUID;

public interface UpdatePetV2 {
    PetV2Dto update(UpdatePetV2Dto dto, UUID authenticatedProfileId, boolean isAdmin);
}
