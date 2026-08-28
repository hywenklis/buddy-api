package com.buddy.api.units.domains.pet.services.v2;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import com.buddy.api.commons.exceptions.PetNotFoundException;
import com.buddy.api.commons.exceptions.UnauthorizedEntityAccessException;
import com.buddy.api.domains.image.repositories.ImageRepository;
import com.buddy.api.domains.pet.dtos.v2.PetV2Dto;
import com.buddy.api.domains.pet.dtos.v2.UpdatePetV2Dto;
import com.buddy.api.domains.pet.entities.PetV2Entity;
import com.buddy.api.domains.pet.enums.PetGender;
import com.buddy.api.domains.pet.enums.PetSpecies;
import com.buddy.api.domains.pet.mappers.v2.PetV2DomainMapper;
import com.buddy.api.domains.pet.repositories.PetV2Repository;
import com.buddy.api.domains.pet.services.v2.impl.UpdatePetV2Impl;
import com.buddy.api.domains.profile.entities.ProfileEntity;
import java.math.BigDecimal;
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
@DisplayName("UpdatePetV2Impl — Unit Tests")
class UpdatePetV2ImplTest {

    @Mock
    private PetV2Repository petV2Repository;

    @Mock
    private ImageRepository imageRepository;

    @Mock
    private PetV2DomainMapper domainMapper;

    @InjectMocks
    private UpdatePetV2Impl updatePetService;

    @Nested
    @DisplayName("update")
    class UpdateTests {

        @Test
        @DisplayName("Should update pet fields when user is the guardian owner")
        void should_update_pet_when_owner() {
            // Arrange
            final var petId = UUID.randomUUID();
            final var profileId = UUID.randomUUID();
            final var guardian = ProfileEntity.builder().profileId(profileId).build();

            final var existing = PetV2Entity.builder()
                .petV2Id(petId)
                .guardianProfile(guardian)
                .name("Old Name")
                .species(PetSpecies.DOG)
                .gender(PetGender.MALE)
                .build();

            final var dto = UpdatePetV2Dto.builder()
                .id(petId)
                .name("New Name")
                .species(PetSpecies.CAT)
                .gender(PetGender.FEMALE)
                .approximateAge(3)
                .size(BigDecimal.valueOf(30))
                .weight(BigDecimal.valueOf(5))
                .isNeutered(true)
                .isForAdoption(false)
                .description("Updated desc")
                .build();

            final var saved = PetV2Entity.builder()
                .petV2Id(petId)
                .name("New Name")
                .build();

            final var expectedDto = PetV2Dto.builder().id(petId).name("New Name").build();

            when(petV2Repository.findById(petId)).thenReturn(Optional.of(existing));
            when(petV2Repository.save(existing)).thenReturn(saved);
            when(imageRepository.findByPetV2OrderByDisplayOrderAsc(saved)).thenReturn(List.of());
            when(domainMapper.toDto(saved, List.of())).thenReturn(expectedDto);

            // Act
            final var result = updatePetService.update(dto, profileId, false);

            // Assert
            assertThat(result).isEqualTo(expectedDto);
            assertThat(existing.getName()).isEqualTo("New Name");
            assertThat(existing.getSpecies()).isEqualTo(PetSpecies.CAT);
            assertThat(existing.getGender()).isEqualTo(PetGender.FEMALE);
            assertThat(existing.getApproximateAge()).isEqualTo(3);
            assertThat(existing.getAgeReportDate()).isNotNull();
            assertThat(existing.getIsNeutered()).isTrue();
            assertThat(existing.getIsForAdoption()).isFalse();
            assertThat(existing.getDescription()).isEqualTo("Updated desc");
        }

        @Test
        @DisplayName("Should update pet when user is admin even if not owner")
        void should_update_pet_when_admin() {
            // Arrange
            final var petId = UUID.randomUUID();
            final var ownerProfileId = UUID.randomUUID();
            final var adminProfileId = UUID.randomUUID();
            final var guardian = ProfileEntity.builder().profileId(ownerProfileId).build();

            final var existing = PetV2Entity.builder()
                .petV2Id(petId)
                .guardianProfile(guardian)
                .name("Old Name")
                .build();

            final var dto = UpdatePetV2Dto.builder()
                .id(petId)
                .name("Admin Updated")
                .build();

            when(petV2Repository.findById(petId)).thenReturn(Optional.of(existing));
            when(petV2Repository.save(existing)).thenReturn(existing);
            when(imageRepository.findByPetV2OrderByDisplayOrderAsc(existing)).thenReturn(List.of());
            when(domainMapper.toDto(existing, List.of())).thenReturn(PetV2Dto.builder().build());

            // Act
            final var result = updatePetService.update(dto, adminProfileId, true);

            // Assert
            assertThat(result).isNotNull();
            assertThat(existing.getName()).isEqualTo("Admin Updated");
        }

        @Test
        @DisplayName("Should throw UnauthorizedEntityAccessException when not owner and not admin")
        void should_throw_unauthorized_when_not_owner() {
            // Arrange
            final var petId = UUID.randomUUID();
            final var ownerProfileId = UUID.randomUUID();
            final var otherProfileId = UUID.randomUUID();
            final var guardian = ProfileEntity.builder().profileId(ownerProfileId).build();

            final var existing = PetV2Entity.builder()
                .petV2Id(petId)
                .guardianProfile(guardian)
                .build();

            final var dto = UpdatePetV2Dto.builder().id(petId).build();

            when(petV2Repository.findById(petId)).thenReturn(Optional.of(existing));

            // Act & Assert
            assertThatThrownBy(() -> updatePetService.update(dto, otherProfileId, false))
                .isInstanceOf(UnauthorizedEntityAccessException.class)
                .hasMessage("Access denied: You do not have permission to update this pet.");
        }

        @Test
        @DisplayName("Should throw PetNotFoundException when pet does not exist")
        void should_throw_not_found_when_pet_missing() {
            final var petId = UUID.randomUUID();
            final var dto = UpdatePetV2Dto.builder().id(petId).build();
            when(petV2Repository.findById(petId)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> updatePetService.update(dto, UUID.randomUUID(), false))
                .isInstanceOf(PetNotFoundException.class);
        }
    }
}
