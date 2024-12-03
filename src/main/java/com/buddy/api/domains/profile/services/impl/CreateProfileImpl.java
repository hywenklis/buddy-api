package com.buddy.api.domains.profile.services.impl;

import com.buddy.api.commons.exceptions.NotFoundException;
import com.buddy.api.domains.account.mappers.AccountMapper;
import com.buddy.api.domains.account.services.FindAccount;
import com.buddy.api.domains.profile.dtos.ProfileDto;
import com.buddy.api.domains.profile.mappers.ProfileMapper;
import com.buddy.api.domains.profile.repositories.ProfileRepository;
import com.buddy.api.domains.profile.services.CreateProfile;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CreateProfileImpl implements CreateProfile {
    private final FindAccount findAccount;
    private final AccountMapper accountMapper;
    private final ProfileMapper profileMapper;
    private final ProfileRepository profileRepository;

    @Override
    @Transactional
    public void create(final ProfileDto profileDto) {
        final var accountId = profileDto.accountId();

        if (!findAccount.existsById(accountId)) {
            throw new NotFoundException("accountId", "Account not found");
        }

        final var account = accountMapper.toAccountEntity(accountId);
        final var profileEntity = profileMapper.toProfileEntity(profileDto, account);

        profileRepository.save(profileEntity);
    }
}
