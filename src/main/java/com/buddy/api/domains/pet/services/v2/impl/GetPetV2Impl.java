package com.buddy.api.domains.pet.services.v2.impl;

import com.buddy.api.commons.exceptions.PetNotFoundException;
import com.buddy.api.domains.image.repositories.ImageRepository;
import com.buddy.api.domains.pet.dtos.v2.PetV2Dto;
import com.buddy.api.domains.pet.mappers.v2.PetV2DomainMapper;
import com.buddy.api.domains.pet.repositories.PetV2Repository;
import com.buddy.api.domains.pet.services.v2.GetPetV2;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class GetPetV2Impl implements GetPetV2 {

    private final PetV2Repository petV2Repository;
    private final ImageRepository imageRepository;
    private final PetV2DomainMapper domainMapper;

    @Override
    @Transactional(readOnly = true)
    public PetV2Dto findById(final UUID id) {
        log.info("Fetching pet details for id '{}'", id);

        final var pet = petV2Repository.findById(id)
            .orElseThrow(() -> new PetNotFoundException(id));

        final var images = imageRepository.findByPetV2OrderByDisplayOrderAsc(pet);
        return domainMapper.toDto(pet, images);
    }
}
