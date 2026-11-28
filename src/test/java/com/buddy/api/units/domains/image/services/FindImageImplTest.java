package com.buddy.api.units.domains.image.services;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.buddy.api.domains.image.entities.ImageEntity;
import com.buddy.api.domains.image.repositories.ImageRepository;
import com.buddy.api.domains.image.services.impl.FindImageImpl;
import com.buddy.api.domains.pet.entities.PetV2Entity;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
@DisplayName("FindImageImpl — Unit Tests")
class FindImageImplTest {

    @Mock
    private ImageRepository imageRepository;

    @InjectMocks
    private FindImageImpl findImageService;

    @Nested
    @DisplayName("findByPetV2OrderByDisplayOrderAsc")
    class FindByPetV2Tests {

        @Test
        @DisplayName("Should return images for given pet entity")
        void should_return_images_when_pet_exists() {
            final var petId = UUID.randomUUID();
            final var pet = PetV2Entity.builder().petV2Id(petId).build();
            final var img = ImageEntity.builder().imageId(UUID.randomUUID()).build();

            when(imageRepository.findByPetV2OrderByDisplayOrderAsc(pet)).thenReturn(List.of(img));

            final var result = findImageService.findByPetV2OrderByDisplayOrderAsc(pet);

            assertThat(result).containsExactly(img);
            verify(imageRepository).findByPetV2OrderByDisplayOrderAsc(pet);
        }

        @Test
        @DisplayName("Should return empty list when pet is null")
        void should_return_empty_when_pet_is_null() {
            final var result = findImageService.findByPetV2OrderByDisplayOrderAsc(null);

            assertThat(result).isEmpty();
            verifyNoInteractions(imageRepository);
        }
    }

    @Nested
    @DisplayName("findByPetV2_PetV2IdInOrderByDisplayOrderAsc")
    class FindByPetV2IdsTests {

        @Test
        @DisplayName("Should return images for given pet IDs list")
        void should_return_images_when_pet_ids_provided() {
            final var petIds = List.of(UUID.randomUUID());
            final var img = ImageEntity.builder().imageId(UUID.randomUUID()).build();

            when(imageRepository.findByPetV2_PetV2IdInOrderByDisplayOrderAsc(petIds))
                .thenReturn(List.of(img));

            final var result = findImageService
                .findByPetV2_PetV2IdInOrderByDisplayOrderAsc(petIds);

            assertThat(result).containsExactly(img);
            verify(imageRepository).findByPetV2_PetV2IdInOrderByDisplayOrderAsc(petIds);
        }

        @Test
        @DisplayName("Should return empty list when pet IDs is null or empty")
        void should_return_empty_when_ids_null_or_empty() {
            assertThat(findImageService.findByPetV2_PetV2IdInOrderByDisplayOrderAsc(null))
                .isEmpty();
            assertThat(findImageService.findByPetV2_PetV2IdInOrderByDisplayOrderAsc(List.of()))
                .isEmpty();
            verifyNoInteractions(imageRepository);
        }
    }
}
