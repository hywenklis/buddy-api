package com.buddy.api.units.domains.pet.services.v2;

import static org.apache.commons.lang3.RandomStringUtils.secure;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.buddy.api.domains.image.entities.ImageEntity;
import com.buddy.api.domains.image.services.FindImage;
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
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;

@ExtendWith(MockitoExtension.class)
@DisplayName("FindPetV2Impl — Unit Tests")
class FindPetV2ImplTest {

    @Mock
    private PetV2Repository petV2Repository;

    @Mock
    private FindImage findImage;

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
            final var criteria = PetV2SearchCriteriaDto.builder().species(PetSpecies.DOG).build();
            final var pageable = PageRequest.of(0, 10);
            final var expectedSort = Sort.by(Sort.Direction.DESC, "creationDate");
            final var expectedPageable = PageRequest.of(0, 10, expectedSort);
            final var emptyPage = new PageImpl<PetV2Entity>(List.of(), expectedPageable, 0);

            when(petV2Repository.findAll(
                (Specification<PetV2Entity>) argThat(spec -> spec != null),
                eq(expectedPageable)
            )).thenReturn(emptyPage);

            final var response = findPetService.findPets(criteria, pageable);

            assertThat(response.content()).isEmpty();
            assertThat(response.totalElements()).isZero();
            verify(petV2Repository).findAll(
                (Specification<PetV2Entity>) argThat(spec -> spec != null),
                eq(expectedPageable)
            );
        }

        @Test
        @DisplayName("Should return paginated pets with associated images")
        void should_return_pets_with_images() {
            final var petId = UUID.randomUUID();
            final var petName = secure().nextAlphabetic(8);
            final var imageUrl = "https://cdn.buddy.com/" + secure().nextAlphabetic(10) + ".jpg";
            final var pet = PetV2Entity.builder().petV2Id(petId).name(petName).build();
            final var criteria = PetV2SearchCriteriaDto.builder().build();
            final var pageable = PageRequest.of(0, 10);
            final var expectedSort = Sort.by(Sort.Direction.DESC, "creationDate");
            final var expectedPageable = PageRequest.of(0, 10, expectedSort);
            final var page = new PageImpl<>(List.of(pet), expectedPageable, 1);

            final var image = ImageEntity.builder()
                .imageId(UUID.randomUUID())
                .petV2(pet)
                .filePath(imageUrl)
                .displayOrder(0)
                .build();

            final var dto = PetV2Dto.builder()
                .id(petId)
                .name(petName)
                .coverImageUrl(imageUrl)
                .build();

            when(petV2Repository.findAll(
                (Specification<PetV2Entity>) argThat(spec -> spec != null),
                eq(expectedPageable)
            )).thenReturn(page);
            when(findImage.findByPetV2_PetV2IdInOrderByDisplayOrderAsc(List.of(petId)))
                .thenReturn(List.of(image));
            when(domainMapper.toDto(pet, List.of(image))).thenReturn(dto);

            final var response = findPetService.findPets(criteria, pageable);

            assertThat(response.content()).containsExactly(dto);
            assertThat(response.totalElements()).isEqualTo(1L);
            assertThat(response.hasNext()).isFalse();
            verify(petV2Repository).findAll(
                (Specification<PetV2Entity>) argThat(spec -> spec != null),
                eq(expectedPageable)
            );
            verify(findImage).findByPetV2_PetV2IdInOrderByDisplayOrderAsc(List.of(petId));
            verify(domainMapper).toDto(pet, List.of(image));
        }

        @Test
        @DisplayName("Should preserve sort and filter images with null petV2")
        void should_preserve_custom_sort_and_filter_images_with_null_pet() {
            final var petId = UUID.randomUUID();
            final var petName = secure().nextAlphabetic(8);
            final var validUrl = "https://cdn.buddy.com/" + secure().nextAlphabetic(10) + ".jpg";
            final var orphanUrl = "https://cdn.buddy.com/" + secure().nextAlphabetic(10) + ".jpg";
            final var pet = PetV2Entity.builder().petV2Id(petId).name(petName).build();
            final var criteria = PetV2SearchCriteriaDto.builder().build();
            final var sort = Sort.by("name").ascending();
            final var pageable = PageRequest.of(0, 10, sort);
            final var page = new PageImpl<>(List.of(pet), pageable, 1);

            final var validImage = ImageEntity.builder()
                .imageId(UUID.randomUUID())
                .petV2(pet)
                .filePath(validUrl)
                .displayOrder(0)
                .build();

            final var orphanedImage = ImageEntity.builder()
                .imageId(UUID.randomUUID())
                .petV2(null)
                .filePath(orphanUrl)
                .displayOrder(1)
                .build();

            final var dto = PetV2Dto.builder()
                .id(petId)
                .name(petName)
                .coverImageUrl(validUrl)
                .build();

            when(petV2Repository.findAll(
                (Specification<PetV2Entity>) argThat(spec -> spec != null),
                eq(pageable)
            )).thenReturn(page);
            when(findImage.findByPetV2_PetV2IdInOrderByDisplayOrderAsc(List.of(petId)))
                .thenReturn(List.of(validImage, orphanedImage));
            when(domainMapper.toDto(pet, List.of(validImage))).thenReturn(dto);

            final var response = findPetService.findPets(criteria, pageable);

            assertThat(response.content()).containsExactly(dto);
            assertThat(response.totalElements()).isEqualTo(1L);
            verify(petV2Repository).findAll(
                (Specification<PetV2Entity>) argThat(spec -> spec != null),
                eq(pageable)
            );
            verify(findImage).findByPetV2_PetV2IdInOrderByDisplayOrderAsc(List.of(petId));
            verify(domainMapper).toDto(pet, List.of(validImage));
        }
    }
}
