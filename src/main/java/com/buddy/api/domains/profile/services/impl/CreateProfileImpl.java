package com.buddy.api.domains.profile.services.impl;

import com.buddy.api.commons.exceptions.InvalidProfileTypeException;
import com.buddy.api.commons.exceptions.NotFoundException;
import com.buddy.api.commons.exceptions.ProfileNameAlreadyRegisteredException;
import com.buddy.api.domains.account.mappers.AccountMapper;
import com.buddy.api.domains.account.services.FindAccount;
import com.buddy.api.domains.profile.dtos.ProfileDto;
import com.buddy.api.domains.profile.enums.ProfileTypeEnum;
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
        validateProfileDto(profileDto);

        final var accountId = profileDto.accountId();
        final var account = accountMapper.toAccountEntity(accountId);
        final var profileEntity = profileMapper.toProfileEntity(profileDto, account);
        profileEntity.setName(profileDto.name().trim());
        profileRepository.save(profileEntity);
    }

    private void validateProfileDto(final ProfileDto profileDto) {
        // TODO: extrair lógica de validação do tipo ADMIN para esquema de autorização

        if (profileDto.profileType() == ProfileTypeEnum.ADMIN) {
            throw new InvalidProfileTypeException(
                "Profile type ADMIN cannot be created by user"
            );
        }

        if (!findAccount.existsById(profileDto.accountId())) {
            throw new NotFoundException("accountId", "Account not found");
        }

        if (profileRepository.existsByName(profileDto.name().trim())) {
            throw new ProfileNameAlreadyRegisteredException("Profile name already registered");
        }
    }
}
