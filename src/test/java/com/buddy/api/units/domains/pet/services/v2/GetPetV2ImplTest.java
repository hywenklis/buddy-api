package com.buddy.api.units.domains.pet.services.v2;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import com.buddy.api.commons.exceptions.PetNotFoundException;
import com.buddy.api.domains.image.entities.ImageEntity;
import com.buddy.api.domains.image.repositories.ImageRepository;
import com.buddy.api.domains.pet.dtos.v2.PetV2Dto;
import com.buddy.api.domains.pet.entities.PetV2Entity;
import com.buddy.api.domains.pet.mappers.v2.PetV2DomainMapper;
import com.buddy.api.domains.pet.repositories.PetV2Repository;
import com.buddy.api.domains.pet.services.v2.impl.GetPetV2Impl;
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
@DisplayName("GetPetV2Impl — Unit Tests")
class GetPetV2ImplTest {

    @Mock
    private PetV2Repository petV2Repository;

    @Mock
    private ImageRepository imageRepository;

    @Mock
    private PetV2DomainMapper domainMapper;

    @InjectMocks
    private GetPetV2Impl getPetService;

    @Nested
    @DisplayName("findById")
    class FindByIdTests {

        @Test
        @DisplayName("Should return PetV2Dto with images when pet is found")
        void should_return_pet_dto_when_found() {
            // Arrange
            final var petId = UUID.randomUUID();
            final var pet = PetV2Entity.builder().petV2Id(petId).name("Bob").build();
            final var image = ImageEntity.builder().imageId(UUID.randomUUID()).build();
            final var expectedDto = PetV2Dto.builder().id(petId).name("Bob").build();

            when(petV2Repository.findById(petId)).thenReturn(Optional.of(pet));
            when(imageRepository.findByPetV2OrderByDisplayOrderAsc(pet)).thenReturn(List.of(image));
            when(domainMapper.toDto(pet, List.of(image))).thenReturn(expectedDto);

            // Act
            final var result = getPetService.findById(petId);

            // Assert
            assertThat(result).isNotNull().isEqualTo(expectedDto);
        }

        @Test
        @DisplayName("Should throw PetNotFoundException when pet is not found")
        void should_throw_when_not_found() {
            // Arrange
            final var petId = UUID.randomUUID();
            when(petV2Repository.findById(petId)).thenReturn(Optional.empty());

            // Act & Assert
            assertThatThrownBy(() -> getPetService.findById(petId))
                .isInstanceOf(PetNotFoundException.class)
                .hasMessage("Pet with id '" + petId + "' was not found.");
        }
    }
}
