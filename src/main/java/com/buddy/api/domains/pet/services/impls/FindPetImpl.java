package com.buddy.api.domains.pet.services.impls;

import com.buddy.api.domains.pet.dtos.PetSearchCriteriaDto;
import com.buddy.api.domains.pet.mappers.PetDomainMapper;
import com.buddy.api.domains.pet.repositories.PetRepository;
import com.buddy.api.domains.pet.services.FindPet;
import com.buddy.api.domains.specifications.PetSpecifications;
import com.buddy.api.web.pets.requests.PetSearchCriteriaRequest;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class FindPetImpl implements FindPet {

    private final PetRepository petRepository;
    private final PetDomainMapper mapper;

    @Override
    public Page<PetSearchCriteriaDto> findPets(PetSearchCriteriaRequest searchParams,
                                               Pageable pageable) {
        return petRepository.findAll(PetSpecifications.withParams(searchParams), pageable)
                .map(mapper::mapParamsToDto);
    }

    @Override
    public List<PetSearchCriteriaDto> findAllPets(PetSearchCriteriaRequest searchParams) {
        return petRepository.findAll(PetSpecifications.withParams(searchParams))
                .stream()
                .map(mapper::mapParamsToDto)
                .collect(Collectors.toList());
    }
}