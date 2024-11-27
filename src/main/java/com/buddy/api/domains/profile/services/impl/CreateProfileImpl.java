package com.buddy.api.domains.profile.services.impl;

import com.buddy.api.commons.exceptions.NotFoundException;
import com.buddy.api.domains.account.repository.AccountRepository;
import com.buddy.api.domains.profile.dtos.ProfileDto;
import com.buddy.api.domains.profile.entities.ProfileEntity;
import com.buddy.api.domains.profile.repositories.ProfileRepository;
import com.buddy.api.domains.profile.services.CreateProfile;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CreateProfileImpl implements CreateProfile {
    private final AccountRepository accountRepository;
    private final ProfileRepository profileRepository;

    @Override
    @Transactional
    public void create(final ProfileDto profileDto) {
        final var account = accountRepository
            .findById(profileDto.accountId())
            .orElseThrow(() -> new NotFoundException("accountId", "Account not found"));

        final var profileEntity = new ProfileEntity(
            null,
            account,
            profileDto.name(),
            profileDto.description(),
            profileDto.bio(),
            profileDto.profileType(),
            false,
            null,
            null
        );

        profileRepository.save(profileEntity);
    }
}
