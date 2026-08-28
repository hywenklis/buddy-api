package com.buddy.api.domains.pet.services.v2.impl;

import com.buddy.api.commons.exceptions.DomainException;
import com.buddy.api.domains.pet.dtos.v2.CreatePetV2Dto;
import com.buddy.api.domains.pet.dtos.v2.PetV2Dto;
import com.buddy.api.domains.pet.mappers.v2.PetV2DomainMapper;
import com.buddy.api.domains.pet.repositories.PetV2Repository;
import com.buddy.api.domains.pet.services.v2.CreatePetV2;
import com.buddy.api.domains.profile.repositories.ProfileRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class CreatePetV2Impl implements CreatePetV2 {

    private final PetV2Repository petV2Repository;
    private final ProfileRepository profileRepository;
    private final PetV2DomainMapper domainMapper;

    @Override
    @Transactional
    public PetV2Dto create(final CreatePetV2Dto dto) {
        log.info("Creating pet '{}' for guardian '{}'", dto.name(), dto.guardianProfileId());

        final var guardianProfile = profileRepository.findById(dto.guardianProfileId())
            .filter(profile -> !Boolean.TRUE.equals(profile.getIsDeleted()))
            .orElseThrow(() -> new DomainException(
                "Guardian profile not found or inactive.",
                "guardianProfileId",
                HttpStatus.UNPROCESSABLE_ENTITY,
                null
            ));

        final var entity = domainMapper.toEntity(dto, guardianProfile);
        final var saved = petV2Repository.save(entity);

        log.info("Pet created successfully with id '{}'", saved.getPetV2Id());
        return domainMapper.toDto(saved);
    }
}
