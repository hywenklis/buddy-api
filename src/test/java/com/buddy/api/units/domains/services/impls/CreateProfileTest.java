package com.buddy.api.units.domains.services.impls;

import static com.buddy.api.builders.account.AccountBuilder.validAccountEntity;
import static com.buddy.api.builders.profile.ProfileBuilder.profileDto;
import static com.buddy.api.utils.RandomStringUtils.generateRandomString;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.buddy.api.commons.exceptions.NotFoundException;
import com.buddy.api.domains.account.repository.AccountRepository;
import com.buddy.api.domains.profile.dtos.ProfileDto;
import com.buddy.api.domains.profile.entities.ProfileEntity;
import com.buddy.api.domains.profile.enums.ProfileTypeEnum;
import com.buddy.api.domains.profile.repositories.ProfileRepository;
import com.buddy.api.domains.profile.services.impl.CreateProfileImpl;
import com.buddy.api.units.UnitTestAbstract;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

public class CreateProfileTest extends UnitTestAbstract {

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private ProfileRepository profileRepository;

    @InjectMocks
    private CreateProfileImpl createProfile;

    @Test
    @DisplayName("Should create a new profile")
    void should_create_new_profile() {
        final var accountId = UUID.randomUUID();
        final var name = generateRandomString(6);
        final var description = generateRandomString(10);
        final var bio = generateRandomString(10);
        final var profileType = ProfileTypeEnum.USER;

        final var validProfileDto = new ProfileDto(
            accountId,
            name,
            description,
            bio,
            profileType
        );

        final var accountEntity = validAccountEntity().accountId(accountId).build();

        final var profileEntity = new ProfileEntity(
            null,
            accountEntity,
            name,
            description,
            bio,
            profileType,
            false,
            null,
            null
        );

        when(accountRepository.findById(accountId)).thenReturn(Optional.of(accountEntity));
        when(profileRepository.save(profileEntity)).thenReturn(profileEntity);

        createProfile.create(validProfileDto);

        verify(profileRepository, times(1)).save(profileEntity);
    }

    @Test
    @DisplayName("Should not create profile when account is not found")
    void should_not_create_profile_when_account_is_not_found() {
        final var accountId = UUID.randomUUID();

        final var invalidProfileDto = profileDto()
            .accountId(accountId)
            .build();

        when(accountRepository.findById(accountId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> createProfile.create(invalidProfileDto))
            .isInstanceOf(NotFoundException.class)
            .usingRecursiveComparison()
            .isEqualTo(new NotFoundException("accountId", "Account not found"));

        verify(accountRepository, times(1)).findById(accountId);
        verify(profileRepository, never()).save(any());
    }
}
