package com.buddy.api.domains.pet.services.v2.impl;

import com.buddy.api.commons.exceptions.ActiveShelterProfileNotFoundException;
import com.buddy.api.domains.account.services.FindAccount;
import com.buddy.api.domains.pet.dtos.v2.CreatePetV2Dto;
import com.buddy.api.domains.pet.dtos.v2.PetV2Dto;
import com.buddy.api.domains.pet.mappers.v2.PetV2DomainMapper;
import com.buddy.api.domains.pet.repositories.PetV2Repository;
import com.buddy.api.domains.pet.services.v2.CreatePetV2;
import com.buddy.api.domains.profile.services.FindProfile;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class CreatePetV2Impl implements CreatePetV2 {

    private final PetV2Repository petV2Repository;
    private final FindAccount findAccount;
    private final FindProfile findProfile;
    private final PetV2DomainMapper domainMapper;

    @Override
    @Transactional
    public PetV2Dto create(final CreatePetV2Dto dto) {
        log.info("Creating pet '{}' for account '{}'", dto.name(), dto.accountId());

        findAccount.findActiveById(dto.accountId());

        final var shelterProfile = findProfile
            .findActiveShelterProfileByAccountId(dto.accountId())
            .orElseThrow(() -> new ActiveShelterProfileNotFoundException(dto.accountId()));

        final var entity = domainMapper.toEntity(dto, shelterProfile);
        final var saved = petV2Repository.save(entity);

        log.info("Pet created successfully with id '{}'", saved.getPetV2Id());
        return domainMapper.toDto(saved);
    }
}
