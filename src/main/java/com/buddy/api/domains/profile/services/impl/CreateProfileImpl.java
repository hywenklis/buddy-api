package com.buddy.api.domains.profile.services.impl;

import com.buddy.api.commons.exceptions.InvalidProfileTypeException;
import com.buddy.api.commons.validation.dtos.ValidationDetailsDto;
import com.buddy.api.commons.validation.impl.ExecuteValidationImpl;
import com.buddy.api.domains.account.mappers.AccountMapper;
import com.buddy.api.domains.account.services.FindAccount;
import com.buddy.api.domains.profile.dtos.ProfileDto;
import com.buddy.api.domains.profile.enums.ProfileTypeEnum;
import com.buddy.api.domains.profile.mappers.ProfileMapper;
import com.buddy.api.domains.profile.repositories.ProfileRepository;
import com.buddy.api.domains.profile.services.CreateProfile;
import jakarta.transaction.Transactional;
import java.util.ArrayList;
import java.util.List;
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
        final var executeValidation = new ExecuteValidationImpl<>();
        executeValidation
            .validate(() -> validateProfileDto(profileDto))
            .andThen(() -> {
                final var accountId = profileDto.accountId();
                final var account = accountMapper.toAccountEntity(accountId);
                final var profileEntity = profileMapper.toProfileEntity(profileDto, account);
                profileEntity.setName(profileDto.name().trim());
                return profileRepository.save(profileEntity);
            });
    }

    private List<ValidationDetailsDto> validateProfileDto(final ProfileDto profileDto) {
        // TODO: extrair lógica de validação do tipo ADMIN para esquema de autorização

        if (profileDto.profileType() == ProfileTypeEnum.ADMIN) {
            throw new InvalidProfileTypeException(
                "Profile type ADMIN cannot be created by user"
            );
        }

        var errors = new ArrayList<ValidationDetailsDto>();

        if (!findAccount.existsById(profileDto.accountId())) {
            errors.add(
                new ValidationDetailsDto("Account not found", "accountId")
            );
        }

        if (profileRepository.existsByName(profileDto.name().trim())) {
            errors.add(
                new ValidationDetailsDto(
                    "Profile name already registered",
                    "name")
            );
        }

        return errors;
    }
}
