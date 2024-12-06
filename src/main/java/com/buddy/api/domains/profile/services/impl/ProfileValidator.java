package com.buddy.api.domains.profile.services.impl;

import com.buddy.api.commons.validation.Validatable;
import com.buddy.api.commons.validation.ValidationCollector;
import com.buddy.api.domains.account.services.FindAccount;
import com.buddy.api.domains.profile.dtos.ProfileDto;
import com.buddy.api.domains.profile.enums.ProfileTypeEnum;
import com.buddy.api.domains.profile.repositories.ProfileRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class ProfileValidator implements Validatable<ProfileDto> {

    private final ValidationCollector collector;
    private final FindAccount findAccount;
    private final ProfileRepository profileRepository;

    @Override
    public void validate(final ProfileDto profileDto) {

        collector.validate(
            () -> profileDto.profileType() == ProfileTypeEnum.ADMIN,
            "profileType",
            "Profile type ADMIN cannot be created by user",
            HttpStatus.BAD_REQUEST
        );

        collector.validate(
            () -> !findAccount.existsById(profileDto.accountId()),
            "accountId",
            "Account not found",
            HttpStatus.NOT_FOUND
        );

        collector.validate(
            () -> profileRepository.existsByName(profileDto.name().trim()),
            "name",
            "Profile name already registered",
            HttpStatus.CONFLICT
        );

        collector.throwIfErrors();
    }
}
