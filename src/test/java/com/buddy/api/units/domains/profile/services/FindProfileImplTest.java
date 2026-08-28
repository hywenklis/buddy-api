package com.buddy.api.units.domains.profile.services;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.buddy.api.domains.profile.dtos.ProfileDto;
import com.buddy.api.domains.profile.entities.ProfileEntity;
import com.buddy.api.domains.profile.enums.ProfileTypeEnum;
import com.buddy.api.domains.profile.mappers.ProfileMapper;
import com.buddy.api.domains.profile.repositories.ProfileRepository;
import com.buddy.api.domains.profile.services.impl.FindProfileImpl;
import com.buddy.api.domains.valueobjects.EmailAddress;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
@DisplayName("FindProfileImpl — Unit Tests")
class FindProfileImplTest {

    @Mock
    private ProfileRepository profileRepository;

    @Mock
    private ProfileMapper profileMapper;

    @InjectMocks
    private FindProfileImpl findProfileService;

    @Nested
    @DisplayName("findByAccountEmail")
    class FindByAccountEmailTests {

        @Test
        @DisplayName("Should return profile dtos list when email exists")
        void should_return_dtos_when_email_exists() {
            final var email = "test@buddy.com";
            final var profileEntity = ProfileEntity.builder().build();
            final var profileDto = ProfileDto.builder().build();

            when(profileRepository.findByAccountEmail(new EmailAddress(email)))
                .thenReturn(Optional.of(List.of(profileEntity)));
            when(profileMapper.toProfilesDto(List.of(profileEntity)))
                .thenReturn(List.of(profileDto));

            final var result = findProfileService.findByAccountEmail(email);

            assertThat(result).containsExactly(profileDto);
            verify(profileRepository).findByAccountEmail(new EmailAddress(email));
        }

        @Test
        @DisplayName("Should return empty list when repository returns empty")
        void should_return_empty_when_not_found() {
            when(profileRepository.findByAccountEmail(any())).thenReturn(Optional.empty());

            final var result = findProfileService.findByAccountEmail("unknown@buddy.com");

            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("findActiveProfilesByAccountId")
    class FindActiveProfilesByAccountIdTests {

        @Test
        @DisplayName("Should return active profiles for account")
        void should_return_active_profiles() {
            final var accountId = UUID.randomUUID();
            final var profile = ProfileEntity.builder().build();

            when(profileRepository.findByAccount_AccountIdAndIsDeletedFalse(accountId))
                .thenReturn(List.of(profile));

            final var result = findProfileService.findActiveProfilesByAccountId(accountId);

            assertThat(result).containsExactly(profile);
            verify(profileRepository).findByAccount_AccountIdAndIsDeletedFalse(accountId);
        }

        @Test
        @DisplayName("Should return empty list when accountId is null")
        void should_return_empty_when_null() {
            final var result = findProfileService.findActiveProfilesByAccountId(null);

            assertThat(result).isEmpty();
            verifyNoInteractions(profileRepository);
        }
    }

    @Nested
    @DisplayName("findActiveProfileByAccountIdAndType")
    class FindActiveProfileByTypeTests {

        @Test
        @DisplayName("Should return active profile for given account and type")
        void should_return_profile_when_found() {
            final var accountId = UUID.randomUUID();
            final var profile = ProfileEntity.builder()
                .profileType(ProfileTypeEnum.SHELTER)
                .isDeleted(false)
                .build();

            when(profileRepository.findByAccount_AccountIdAndProfileTypeAndIsDeletedFalse(
                accountId, ProfileTypeEnum.SHELTER
            )).thenReturn(Optional.of(profile));

            final var result = findProfileService
                .findActiveProfileByAccountIdAndType(accountId, ProfileTypeEnum.SHELTER);

            assertThat(result).contains(profile);
        }

        @Test
        @DisplayName("Should return empty when accountId or profileType is null")
        void should_return_empty_when_null() {
            assertThat(findProfileService.findActiveProfileByAccountIdAndType(null, null))
                .isEmpty();
            assertThat(findProfileService.findActiveProfileByAccountIdAndType(
                UUID.randomUUID(), null
            )).isEmpty();
            verifyNoInteractions(profileRepository);
        }
    }

    @Nested
    @DisplayName("findActiveShelterProfileByAccountId")
    class FindActiveShelterProfileTests {

        @Test
        @DisplayName("Should return active shelter profile")
        void should_return_shelter_profile() {
            final var accountId = UUID.randomUUID();
            final var profile = ProfileEntity.builder()
                .profileType(ProfileTypeEnum.SHELTER)
                .isDeleted(false)
                .build();

            when(profileRepository.findByAccount_AccountIdAndProfileTypeAndIsDeletedFalse(
                accountId, ProfileTypeEnum.SHELTER
            )).thenReturn(Optional.of(profile));

            final var result = findProfileService.findActiveShelterProfileByAccountId(accountId);

            assertThat(result).contains(profile);
        }
    }

    @Nested
    @DisplayName("findActiveByIdAndAccountId")
    class FindActiveByIdAndAccountTests {

        @Test
        @DisplayName("Should return profile matching profileId and accountId")
        void should_return_profile_when_matching() {
            final var profileId = UUID.randomUUID();
            final var accountId = UUID.randomUUID();
            final var profile = ProfileEntity.builder().build();

            when(profileRepository.findByProfileIdAndAccount_AccountIdAndIsDeletedFalse(
                profileId, accountId
            )).thenReturn(Optional.of(profile));

            final var result = findProfileService.findActiveByIdAndAccountId(profileId, accountId);

            assertThat(result).contains(profile);
        }

        @Test
        @DisplayName("Should return empty when profileId or accountId is null")
        void should_return_empty_when_null() {
            assertThat(findProfileService.findActiveByIdAndAccountId(null, null)).isEmpty();
            assertThat(findProfileService.findActiveByIdAndAccountId(UUID.randomUUID(), null))
                .isEmpty();
            verifyNoInteractions(profileRepository);
        }
    }

    @Nested
    @DisplayName("findById")
    class FindByIdTests {

        @Test
        @DisplayName("Should return profile entity when profile ID exists")
        void should_return_profile_when_id_exists() {
            final var profileId = UUID.randomUUID();
            final var entity = ProfileEntity.builder().build();

            when(profileRepository.findById(profileId)).thenReturn(Optional.of(entity));

            final var result = findProfileService.findById(profileId);

            assertThat(result).contains(entity);
            verify(profileRepository).findById(profileId);
        }

        @Test
        @DisplayName("Should return empty optional when profileId is null")
        void should_return_empty_when_id_null() {
            final var result = findProfileService.findById(null);

            assertThat(result).isEmpty();
            verifyNoInteractions(profileRepository);
        }
    }
}
