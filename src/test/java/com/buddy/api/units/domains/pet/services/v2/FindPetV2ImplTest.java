package com.buddy.api.units.domains.pet.services.v2;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import com.buddy.api.domains.image.entities.ImageEntity;
import com.buddy.api.domains.image.repositories.ImageRepository;
import com.buddy.api.domains.pet.dtos.v2.PetV2Dto;
import com.buddy.api.domains.pet.dtos.v2.PetV2SearchCriteriaDto;
import com.buddy.api.domains.pet.entities.PetV2Entity;
import com.buddy.api.domains.pet.enums.PetSpecies;
import com.buddy.api.domains.pet.mappers.v2.PetV2DomainMapper;
import com.buddy.api.domains.pet.repositories.PetV2Repository;
import com.buddy.api.domains.pet.services.v2.impl.FindPetV2Impl;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

@ExtendWith(MockitoExtension.class)
@DisplayName("FindPetV2Impl — Unit Tests")
class FindPetV2ImplTest {

    @Mock
    private PetV2Repository petV2Repository;

    @Mock
    private ImageRepository imageRepository;

    @Mock
    private PetV2DomainMapper domainMapper;

    @InjectMocks
    private FindPetV2Impl findPetService;

    @Nested
    @DisplayName("findPets")
    class FindPetsTests {

        @Test
        @DisplayName("Should return empty PageResponse when no pets match criteria")
        void should_return_empty_page_when_no_pets() {
            // Arrange
            final var criteria = PetV2SearchCriteriaDto.builder().species(PetSpecies.DOG).build();
            final var pageable = PageRequest.of(0, 10);
            final var emptyPage = new PageImpl<PetV2Entity>(List.of(), pageable, 0);

            when(petV2Repository.findAll(any(Specification.class), any(Pageable.class)))
                .thenReturn(emptyPage);

            // Act
            final var response = findPetService.findPets(criteria, pageable);

            // Assert
            assertThat(response.content()).isEmpty();
            assertThat(response.totalElements()).isZero();
        }

        @Test
        @DisplayName("Should return paginated pets with associated images")
        void should_return_pets_with_images() {
            // Arrange
            final var petId = UUID.randomUUID();
            final var pet = PetV2Entity.builder().petV2Id(petId).name("Thor").build();
            final var criteria = PetV2SearchCriteriaDto.builder().build();
            final var pageable = PageRequest.of(0, 10);
            final var page = new PageImpl<>(List.of(pet), pageable, 1);

            final var image = ImageEntity.builder()
                .imageId(UUID.randomUUID())
                .petV2(pet)
                .filePath("http://cdn.com/thor.jpg")
                .displayOrder(0)
                .build();

            final var dto = PetV2Dto.builder()
                .id(petId)
                .name("Thor")
                .coverImageUrl("http://cdn.com/thor.jpg")
                .build();

            when(petV2Repository.findAll(any(Specification.class), any(Pageable.class)))
                .thenReturn(page);
            when(imageRepository.findByPetV2_PetV2IdInOrderByDisplayOrderAsc(List.of(petId)))
                .thenReturn(List.of(image));
            when(domainMapper.toDto(eq(pet), any())).thenReturn(dto);

            // Act
            final var response = findPetService.findPets(criteria, pageable);

            // Assert
            assertThat(response.content()).containsExactly(dto);
            assertThat(response.totalElements()).isEqualTo(1L);
            assertThat(response.hasNext()).isFalse();
        }
    }
}
