package com.buddy.api.units.domains.pet.services.v2;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.buddy.api.commons.exceptions.AccountBlockedException;
import com.buddy.api.commons.exceptions.ActiveShelterProfileNotFoundException;
import com.buddy.api.domains.account.dtos.AccountDto;
import com.buddy.api.domains.account.services.FindAccount;
import com.buddy.api.domains.pet.dtos.v2.CreatePetV2Dto;
import com.buddy.api.domains.pet.dtos.v2.PetV2Dto;
import com.buddy.api.domains.pet.entities.PetV2Entity;
import com.buddy.api.domains.pet.enums.PetGender;
import com.buddy.api.domains.pet.enums.PetSpecies;
import com.buddy.api.domains.pet.mappers.v2.PetV2DomainMapper;
import com.buddy.api.domains.pet.repositories.PetV2Repository;
import com.buddy.api.domains.pet.services.v2.impl.CreatePetV2Impl;
import com.buddy.api.domains.profile.entities.ProfileEntity;
import com.buddy.api.domains.profile.enums.ProfileTypeEnum;
import com.buddy.api.domains.profile.services.FindProfile;
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
@DisplayName("CreatePetV2Impl — Unit Tests")
class CreatePetV2ImplTest {

    @Mock
    private PetV2Repository petV2Repository;

    @Mock
    private FindAccount findAccount;

    @Mock
    private FindProfile findProfile;

    @Mock
    private PetV2DomainMapper domainMapper;

    @InjectMocks
    private CreatePetV2Impl createPetService;

    @Nested
    @DisplayName("create")
    class CreateTests {

        @Test
        @DisplayName("Should create pet successfully when account and shelter profile are active")
        void should_create_pet_successfully() {
            // Arrange
            final var accountId = UUID.randomUUID();
            final var petId = UUID.randomUUID();
            final var dto = CreatePetV2Dto.builder()
                .accountId(accountId)
                .name("Max")
                .species(PetSpecies.DOG)
                .gender(PetGender.MALE)
                .isForAdoption(true)
                .build();

            final var shelterProfile = ProfileEntity.builder()
                .profileId(UUID.randomUUID())
                .profileType(ProfileTypeEnum.SHELTER)
                .isDeleted(false)
                .build();

            final var entity = PetV2Entity.builder()
                .name("Max")
                .build();

            final var savedEntity = PetV2Entity.builder()
                .petV2Id(petId)
                .name("Max")
                .build();

            final var expectedDto = PetV2Dto.builder()
                .id(petId)
                .name("Max")
                .build();

            when(findAccount.findActiveById(accountId))
                .thenReturn(AccountDto.builder().accountId(accountId).build());
            when(findProfile.findActiveShelterProfileByAccountId(accountId))
                .thenReturn(Optional.of(shelterProfile));
            when(domainMapper.toEntity(dto, shelterProfile)).thenReturn(entity);
            when(petV2Repository.save(entity)).thenReturn(savedEntity);
            when(domainMapper.toDto(savedEntity)).thenReturn(expectedDto);

            // Act
            final var result = createPetService.create(dto);

            // Assert
            assertThat(result).isNotNull().isEqualTo(expectedDto);
            verify(findAccount).findActiveById(accountId);
            verify(findProfile).findActiveShelterProfileByAccountId(accountId);
            verify(petV2Repository).save(entity);
        }

        @Test
        @DisplayName("Should throw when shelter profile not found")
        void should_throw_when_shelter_profile_not_found() {
            // Arrange
            final var accountId = UUID.randomUUID();
            final var dto = CreatePetV2Dto.builder()
                .accountId(accountId)
                .build();

            when(findAccount.findActiveById(accountId))
                .thenReturn(AccountDto.builder().accountId(accountId).build());
            when(findProfile.findActiveShelterProfileByAccountId(accountId))
                .thenReturn(Optional.empty());

            // Act & Assert
            assertThatThrownBy(() -> createPetService.create(dto))
                .isInstanceOf(ActiveShelterProfileNotFoundException.class)
                .hasMessageContaining("Active shelter profile not found for account");
        }

        @Test
        @DisplayName("Should propagate exception when account is not active")
        void should_throw_when_account_not_active() {
            final var accountId = UUID.randomUUID();
            final var dto = CreatePetV2Dto.builder().accountId(accountId).build();

            when(findAccount.findActiveById(accountId))
                .thenThrow(new AccountBlockedException("accountId", "Account is blocked"));

            assertThatThrownBy(() -> createPetService.create(dto))
                .isInstanceOf(AccountBlockedException.class)
                .hasMessage("Account is blocked");
        }
    }
}
