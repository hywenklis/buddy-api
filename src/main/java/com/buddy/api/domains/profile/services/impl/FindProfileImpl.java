package com.buddy.api.domains.profile.services.impl;

import com.buddy.api.domains.profile.dtos.ProfileDto;
import com.buddy.api.domains.profile.entities.ProfileEntity;
import com.buddy.api.domains.profile.enums.ProfileTypeEnum;
import com.buddy.api.domains.profile.mappers.ProfileMapper;
import com.buddy.api.domains.profile.repositories.ProfileRepository;
import com.buddy.api.domains.profile.services.FindProfile;
import com.buddy.api.domains.valueobjects.EmailAddress;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class FindProfileImpl implements FindProfile {

    private final ProfileRepository profileRepository;
    private final ProfileMapper profileMapper;

    @Override
    @Transactional(readOnly = true)
    public List<ProfileDto> findByAccountEmail(final EmailAddress email) {
        log.info("Searching profiles by account email");
        if (email == null) {
            return Collections.emptyList();
        }
        return profileRepository.findByAccountEmail(email)
            .map(profileMapper::toProfilesDto)
            .orElse(Collections.emptyList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProfileEntity> findActiveProfilesByAccountId(final UUID accountId) {
        log.info("Searching active profiles by account ID '{}'", accountId);
        if (accountId == null) {
            return List.of();
        }
        return profileRepository.findByAccount_AccountIdAndIsDeletedFalse(accountId);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<ProfileEntity> findActiveProfileByAccountIdAndType(
        final UUID accountId,
        final ProfileTypeEnum profileType
    ) {
        log.info("Searching active profile by account ID '{}' and type '{}'",
            accountId, profileType);
        if (accountId == null || profileType == null) {
            return Optional.empty();
        }
        return profileRepository
            .findByAccount_AccountIdAndProfileTypeAndIsDeletedFalse(accountId, profileType);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<ProfileEntity> findActiveShelterProfileByAccountId(final UUID accountId) {
        log.info("Searching active shelter profile by account ID '{}'", accountId);
        return findActiveProfileByAccountIdAndType(accountId, ProfileTypeEnum.SHELTER);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<ProfileEntity> findActiveByIdAndAccountId(
        final UUID profileId,
        final UUID accountId
    ) {
        log.info("Searching active profile by profile ID '{}' and account ID '{}'",
            profileId, accountId);
        if (profileId == null || accountId == null) {
            return Optional.empty();
        }
        return profileRepository
            .findByProfileIdAndAccount_AccountIdAndIsDeletedFalse(profileId, accountId);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<ProfileEntity> findById(final UUID profileId) {
        log.info("Searching profile by profile ID '{}'", profileId);
        if (profileId == null) {
            return Optional.empty();
        }
        return profileRepository.findById(profileId);
    }
}
