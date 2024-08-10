package com.buddy.api.domains.pet.services.impls;

import com.buddy.api.domains.page.PageableBuilder;
import com.buddy.api.domains.pet.dtos.PetSearchCriteriaDto;
import com.buddy.api.domains.pet.entities.PetEntity;
import com.buddy.api.domains.pet.mappers.PetDomainMapper;
import com.buddy.api.domains.pet.repositories.PetRepository;
import com.buddy.api.domains.pet.services.FindPet;
import com.buddy.api.domains.specifications.PetSpecifications;
import com.buddy.api.web.pets.requests.PetSearchCriteriaRequest;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.hibernate.Hibernate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class FindPetImpl implements FindPet {

    private final PetRepository petRepository;
    private final PetDomainMapper mapper;

    @Override
    @Transactional(readOnly = true)
    public Page<PetSearchCriteriaDto> findPets(final PetSearchCriteriaRequest searchParams,
                                               final Pageable pageable) {
        Specification<PetEntity> spec = PetSpecifications.withParams(searchParams);
        Pageable adjustedPageable = PageableBuilder.buildPageable(pageable);
        Page<PetEntity> petPage = petRepository.findAll(spec, adjustedPageable);
        petPage.forEach(this::initializeProxies);
        return petPage.map(mapper::mapParamsToDto);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PetSearchCriteriaDto> findAllPets(final PetSearchCriteriaRequest searchParams) {
        Specification<PetEntity> spec = PetSpecifications.withParams(searchParams);
        Sort sort = Sort.by(Sort.Direction.DESC, PageableBuilder.DEFAULT_SORT_PROPERTY);
        List<PetEntity> petEntities = petRepository.findAll(spec, sort);
        petEntities.forEach(this::initializeProxies);
        return petEntities.stream()
            .map(mapper::mapParamsToDto)
            .toList();
    }

    private void initializeProxies(PetEntity pet) {
        if (pet.getImages() != null) {
            Hibernate.initialize(pet.getImages());
        }
        if (pet.getShelter() != null) {
            Hibernate.initialize(pet.getShelter());
        }
    }
}
