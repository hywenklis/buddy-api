package com.buddy.api.units.domains.pet.services.v2;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.buddy.api.commons.exceptions.DomainException;
import com.buddy.api.domains.pet.dtos.v2.CreatePetV2Dto;
import com.buddy.api.domains.pet.dtos.v2.PetV2Dto;
import com.buddy.api.domains.pet.entities.PetV2Entity;
import com.buddy.api.domains.pet.enums.PetGender;
import com.buddy.api.domains.pet.enums.PetSpecies;
import com.buddy.api.domains.pet.mappers.v2.PetV2DomainMapper;
import com.buddy.api.domains.pet.repositories.PetV2Repository;
import com.buddy.api.domains.pet.services.v2.impl.CreatePetV2Impl;
import com.buddy.api.domains.profile.entities.ProfileEntity;
import com.buddy.api.domains.profile.repositories.ProfileRepository;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

@ExtendWith(MockitoExtension.class)
@DisplayName("CreatePetV2Impl — Unit Tests")
class CreatePetV2ImplTest {

    @Mock
    private PetV2Repository petV2Repository;

    @Mock
    private ProfileRepository profileRepository;

    @Mock
    private PetV2DomainMapper domainMapper;

    @InjectMocks
    private CreatePetV2Impl createPetService;

    @Nested
    @DisplayName("create")
    class CreateTests {

        @Test
        @DisplayName("Should create pet successfully when guardian profile is active")
        void should_create_pet_successfully() {
            // Arrange
            final var profileId = UUID.randomUUID();
            final var petId = UUID.randomUUID();
            final var dto = CreatePetV2Dto.builder()
                .guardianProfileId(profileId)
                .name("Max")
                .species(PetSpecies.DOG)
                .gender(PetGender.MALE)
                .isForAdoption(true)
                .build();

            final var profile = ProfileEntity.builder()
                .profileId(profileId)
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

            when(profileRepository.findById(profileId)).thenReturn(Optional.of(profile));
            when(domainMapper.toEntity(dto, profile)).thenReturn(entity);
            when(petV2Repository.save(entity)).thenReturn(savedEntity);
            when(domainMapper.toDto(savedEntity)).thenReturn(expectedDto);

            // Act
            final var result = createPetService.create(dto);

            // Assert
            assertThat(result).isNotNull().isEqualTo(expectedDto);
            verify(petV2Repository).save(entity);
        }

        @Test
        @DisplayName("Should throw DomainException (422) when guardian profile not found")
        void should_throw_when_profile_not_found() {
            // Arrange
            final var profileId = UUID.randomUUID();
            final var dto = CreatePetV2Dto.builder()
                .guardianProfileId(profileId)
                .build();

            when(profileRepository.findById(profileId)).thenReturn(Optional.empty());

            // Act & Assert
            assertThatThrownBy(() -> createPetService.create(dto))
                .isInstanceOf(DomainException.class)
                .hasMessage("Guardian profile not found or inactive.")
                .satisfies(ex -> {
                    final var domainEx = (DomainException) ex;
                    assertThat(domainEx.getHttpStatus()).isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY);
                });
        }

        @Test
        @DisplayName("Should throw DomainException (422) when guardian profile is deleted")
        void should_throw_when_profile_is_deleted() {
            // Arrange
            final var profileId = UUID.randomUUID();
            final var dto = CreatePetV2Dto.builder()
                .guardianProfileId(profileId)
                .build();

            final var deletedProfile = ProfileEntity.builder()
                .profileId(profileId)
                .isDeleted(true)
                .build();

            when(profileRepository.findById(profileId)).thenReturn(Optional.of(deletedProfile));

            // Act & Assert
            assertThatThrownBy(() -> createPetService.create(dto))
                .isInstanceOf(DomainException.class)
                .hasMessage("Guardian profile not found or inactive.");
        }
    }
}
