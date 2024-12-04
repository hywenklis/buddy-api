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
import com.buddy.api.commons.exceptions.InvalidProfileTypeException;
import com.buddy.api.commons.exceptions.NotFoundException;
import com.buddy.api.domains.account.mappers.AccountMapper;
import com.buddy.api.domains.account.services.FindAccount;
import com.buddy.api.domains.profile.entities.ProfileEntity;
import com.buddy.api.domains.profile.enums.ProfileTypeEnum;
import com.buddy.api.domains.profile.mappers.ProfileMapper;
import com.buddy.api.domains.profile.repositories.ProfileRepository;
import com.buddy.api.domains.profile.services.impl.CreateProfileImpl;
import com.buddy.api.units.UnitTestAbstract;
import java.util.Comparator;
import java.util.Objects;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;

class CreateProfileTest extends UnitTestAbstract {

    @Mock
    private FindAccount findAccount;

    @Spy
    private AccountMapper accountMapper = Mappers.getMapper(AccountMapper.class);

    @Spy
    private ProfileMapper profileMapper = Mappers.getMapper(ProfileMapper.class);

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

        when(findAccount.existsById(accountId)).thenReturn(true);
        when(profileRepository.save(profileEntity)).thenReturn(profileEntity);

        createProfile.create(validProfileDto);

        verify(profileRepository, times(1))
            .save(profileEntityCaptor.capture());

        assertThat(profileEntity)
            .usingRecursiveComparison()
            .withComparatorForType(Comparator.comparing(Objects::nonNull), UUID.class)
            .ignoringExpectedNullFields()
            .isEqualTo(profileEntityCaptor.getValue());
    }

    @Test
    @DisplayName("Should not create profile when account is not found")
    void should_not_create_profile_when_account_is_not_found() {
        final var accountId = UUID.randomUUID();

        final var invalidProfileDto = profileDto()
            .accountId(accountId)
            .build();

        when(findAccount.existsById(accountId)).thenReturn(false);

        assertThatThrownBy(() -> createProfile.create(invalidProfileDto))
            .isInstanceOf(NotFoundException.class)
            .hasMessage("Account not found")
            .extracting("fieldName")
            .isEqualTo("accountId");

        verify(profileRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should not create ADMIN profile")
    void should_not_create_admin_profile() {
        // TODO: extrair lógica de validação do tipo ADMIN para esquema de autorização

        final var invalidProfileDto = profileDto()
            .profileType(ProfileTypeEnum.ADMIN)
            .build();

        assertThatThrownBy(() -> createProfile.create(invalidProfileDto))
            .isInstanceOf(InvalidProfileTypeException.class)
            .hasMessage("Profile type ADMIN cannot be created by user")
            .extracting("fieldName")
            .isEqualTo("profileType");

        verify(profileRepository, never()).save(any());
    }
}
