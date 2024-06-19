package com.buddy.api.domains.pet.services;

import com.buddy.api.domains.pet.dtos.PetSearchCriteriaDto;
import com.buddy.api.web.pets.requests.PetSearchCriteriaRequest;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface FindPet {
    Page<PetSearchCriteriaDto> findPets(PetSearchCriteriaRequest searchParams, Pageable pageable);

    List<PetSearchCriteriaDto> findAllPets(PetSearchCriteriaRequest searchParams);
}
