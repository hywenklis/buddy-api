package com.buddy.api.domains.pet.services.v2;

import com.buddy.api.commons.page.PageResponse;
import com.buddy.api.domains.pet.dtos.v2.PetV2Dto;
import com.buddy.api.domains.pet.dtos.v2.PetV2SearchCriteriaDto;
import org.springframework.data.domain.Pageable;

public interface FindPetV2 {
    PageResponse<PetV2Dto> findPets(PetV2SearchCriteriaDto criteria, Pageable pageable);
}
