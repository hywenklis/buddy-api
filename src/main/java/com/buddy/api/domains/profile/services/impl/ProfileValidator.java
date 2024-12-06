package com.buddy.api.domains.profile.services.impl;

import com.buddy.api.commons.exceptions.InvalidProfileTypeException;
import com.buddy.api.commons.exceptions.NotFoundException;
import com.buddy.api.commons.exceptions.ProfileNameAlreadyRegisteredException;
import com.buddy.api.commons.validation.Validator;
import com.buddy.api.domains.account.services.FindAccount;
import com.buddy.api.domains.profile.dtos.ProfileDto;
import com.buddy.api.domains.profile.enums.ProfileTypeEnum;
import com.buddy.api.domains.profile.repositories.ProfileRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class ProfileValidator {

    private final Validator validator;
    private final FindAccount findAccount;
    private final ProfileRepository profileRepository;

    public void validate(final ProfileDto profileDto) {

        validator.validate(
            () -> profileDto.profileType() == ProfileTypeEnum.ADMIN,
            new InvalidProfileTypeException(profileDto.profileType())
        );

        validator.validate(
            () -> !findAccount.existsById(profileDto.accountId()),
            new NotFoundException(
                "accountId",
                "Account not found with ID: " + profileDto.accountId()
            )
        );

        validator.validate(
            () -> profileRepository.existsByName(profileDto.name().trim()),
            new ProfileNameAlreadyRegisteredException(profileDto.name().trim())
        );

        validator.throwIfErrors();
    }
}
