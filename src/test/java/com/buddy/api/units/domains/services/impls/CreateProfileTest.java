package com.buddy.api.units.domains.services.impls;

import static com.buddy.api.builders.account.AccountBuilder.validAccountEntity;
import static com.buddy.api.builders.profile.ProfileBuilder.profileDto;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.buddy.api.builders.profile.ProfileBuilder;
import com.buddy.api.commons.exceptions.NotFoundException;
import com.buddy.api.domains.account.repository.AccountRepository;
import com.buddy.api.domains.profile.entities.ProfileEntity;
import com.buddy.api.domains.profile.repositories.ProfileRepository;
import com.buddy.api.domains.profile.services.impl.CreateProfileImpl;
import com.buddy.api.units.UnitTestAbstract;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
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

        final var validProfileDto = profileDto().accountId(accountId).build();

        final var accountEntity = validAccountEntity().accountId(accountId).build();

        final var profileEntity = ProfileBuilder.profileEntity()
            .account(accountEntity)
            .name(validProfileDto.name())
            .description(validProfileDto.description())
            .bio(validProfileDto.bio())
            .profileType(validProfileDto.profileType())
            .build();

        final var profileEntityCaptor = ArgumentCaptor.forClass(ProfileEntity.class);

        when(accountRepository.findById(accountId)).thenReturn(Optional.of(accountEntity));
        when(profileRepository.save(profileEntity)).thenReturn(profileEntity);

        createProfile.create(validProfileDto);

        verify(profileRepository, times(1))
            .save(profileEntityCaptor.capture());

        assertThat(profileEntity)
            .usingRecursiveComparison()
            .isEqualTo(profileEntityCaptor.getValue());
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
            .hasMessage("Account not found")
            .extracting("fieldName")
            .isEqualTo("accountId");

        verify(accountRepository, times(1)).findById(accountId);
        verify(profileRepository, never()).save(any());
    }
}
