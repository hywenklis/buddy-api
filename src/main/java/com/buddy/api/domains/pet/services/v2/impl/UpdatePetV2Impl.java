package com.buddy.api.domains.pet.services.v2.impl;

import com.buddy.api.commons.exceptions.ActiveShelterProfileNotFoundException;
import com.buddy.api.commons.exceptions.PetNotFoundException;
import com.buddy.api.commons.exceptions.UnauthorizedEntityAccessException;
import com.buddy.api.domains.account.services.FindAccount;
import com.buddy.api.domains.image.services.FindImage;
import com.buddy.api.domains.pet.dtos.v2.PetV2Dto;
import com.buddy.api.domains.pet.dtos.v2.UpdatePetV2Dto;
import com.buddy.api.domains.pet.mappers.v2.PetV2DomainMapper;
import com.buddy.api.domains.pet.repositories.PetV2Repository;
import com.buddy.api.domains.pet.services.v2.UpdatePetV2;
import com.buddy.api.domains.profile.entities.ProfileEntity;
import com.buddy.api.domains.profile.services.FindProfile;
import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class UpdatePetV2Impl implements UpdatePetV2 {

    private final PetV2Repository petV2Repository;
    private final FindAccount findAccount;
    private final FindProfile findProfile;
    private final FindImage findImage;
    private final PetV2DomainMapper domainMapper;

    @Override
    @Transactional
    public PetV2Dto update(final UpdatePetV2Dto dto,
                           final UUID authenticatedAccountId,
                           final boolean isAdmin) {
        log.info("Updating pet '{}' by account '{}' (admin: {})",
            dto.id(), authenticatedAccountId, isAdmin);

        final var pet = petV2Repository.findById(dto.id())
            .orElseThrow(() -> new PetNotFoundException(dto.id()));

        findAccount.findActiveById(authenticatedAccountId);

        if (!isAdmin) {
            final var activeShelterProfile = findProfile
                .findActiveShelterProfileByAccountId(authenticatedAccountId)
                .orElseThrow(
                    () -> new ActiveShelterProfileNotFoundException(authenticatedAccountId));

            validateOwnership(pet.getGuardianProfile(), activeShelterProfile.getProfileId());
        }

        Optional.ofNullable(dto.name()).ifPresent(pet::setName);
        Optional.ofNullable(dto.species()).ifPresent(pet::setSpecies);
        Optional.ofNullable(dto.gender()).ifPresent(pet::setGender);
        Optional.ofNullable(dto.size()).ifPresent(pet::setSize);
        Optional.ofNullable(dto.weight()).ifPresent(pet::setWeight);
        Optional.ofNullable(dto.isNeutered()).ifPresent(pet::setIsNeutered);
        Optional.ofNullable(dto.isForAdoption()).ifPresent(pet::setIsForAdoption);
        Optional.ofNullable(dto.description()).ifPresent(pet::setDescription);

        Optional.ofNullable(dto.approximateAge()).ifPresent(age -> {
            pet.setApproximateAge(age);
            pet.setAgeReportDate(LocalDate.now());
        });

        final var saved = petV2Repository.save(pet);
        final var images = findImage.findByPetV2OrderByDisplayOrderAsc(saved);

        log.info("Pet '{}' updated successfully", saved.getPetV2Id());
        return domainMapper.toDto(saved, images);
    }

    private void validateOwnership(final ProfileEntity guardian, final UUID accountProfileId) {
        Optional.ofNullable(guardian)
            .filter(profile -> profile.getProfileId().equals(accountProfileId))
            .orElseThrow(() -> new UnauthorizedEntityAccessException(
                "Access denied: You do not have permission to update this pet."
            ));
    }
}
