package com.buddy.api.units.domains.pet.mappers;

import static org.apache.commons.lang3.RandomStringUtils.secure;
import static org.assertj.core.api.Assertions.assertThat;

import com.buddy.api.domains.image.entities.ImageEntity;
import com.buddy.api.domains.pet.dtos.v2.CreatePetV2Dto;
import com.buddy.api.domains.pet.enums.PetGender;
import com.buddy.api.domains.pet.enums.PetSpecies;
import com.buddy.api.domains.pet.mappers.v2.PetV2DomainMapper;
import com.buddy.api.domains.pet.mappers.v2.PetV2DomainMapperImpl;
import com.buddy.api.domains.profile.entities.ProfileEntity;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

@DisplayName("PetV2DomainMapper — Unit Tests")
class PetV2DomainMapperTest {

    private PetV2DomainMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = new PetV2DomainMapperImpl();
    }

    @Nested
    @DisplayName("toEntity")
    class ToEntityTests {

        @Test
        @DisplayName("Should map CreatePetV2Dto to PetV2Entity and compute ageReportDate")
        void should_map_create_dto_to_entity() {
            final var petName = secure().nextAlphabetic(8);
            final var description = secure().nextAlphabetic(20);
            final var profile = ProfileEntity.builder().profileId(UUID.randomUUID()).build();
            final var dto = CreatePetV2Dto.builder()
                .accountId(UUID.randomUUID())
                .name(petName)
                .species(PetSpecies.DOG)
                .gender(PetGender.MALE)
                .approximateAge(3)
                .size(BigDecimal.valueOf(45.5))
                .weight(BigDecimal.valueOf(12.0))
                .isNeutered(true)
                .isForAdoption(true)
                .description(description)
                .build();

            final var entity = mapper.toEntity(dto, profile);

            assertThat(entity).isNotNull();
            assertThat(entity.getName()).isEqualTo(petName);
            assertThat(entity.getSpecies()).isEqualTo(PetSpecies.DOG);
            assertThat(entity.getGender()).isEqualTo(PetGender.MALE);
            assertThat(entity.getApproximateAge()).isEqualTo(3);
            assertThat(entity.getAgeReportDate()).isEqualTo(LocalDate.now());
            assertThat(entity.getSize()).isEqualTo(BigDecimal.valueOf(45.5));
            assertThat(entity.getWeight()).isEqualTo(BigDecimal.valueOf(12.0));
            assertThat(entity.getIsNeutered()).isTrue();
            assertThat(entity.getIsForAdoption()).isTrue();
            assertThat(entity.getDescription()).isEqualTo(description);
            assertThat(entity.getGuardianProfile()).isEqualTo(profile);
        }

        @Test
        @DisplayName("Should return null when dto and profile are null")
        void should_return_null_when_null() {
            assertThat(mapper.toEntity(null, null)).isNull();
        }
    }

    @Nested
    @DisplayName("toDto")
    class ToDtoTests {

        @Test
        @DisplayName("Should map PetV2Entity with images extracting cover image by lowest order")
        void should_map_entity_to_dto_with_cover_image() {
            final var petName = secure().nextAlphabetic(8);
            final var profileId = UUID.randomUUID();
            final var profile = ProfileEntity.builder().profileId(profileId).build();
            final var dto = CreatePetV2Dto.builder()
                .name(petName)
                .species(PetSpecies.CAT)
                .gender(PetGender.FEMALE)
                .build();

            final var entity = mapper.toEntity(dto, profile);
            final var urlCover = "https://cdn.buddy.com/" + secure().nextAlphabetic(10) + ".jpg";
            final var urlSecondary = "https://cdn.buddy.com/" + secure().nextAlphabetic(10) + ".jpg";

            final var img1 = ImageEntity.builder()
                .imageId(UUID.randomUUID())
                .filePath(urlSecondary)
                .displayOrder(2)
                .build();

            final var img2 = ImageEntity.builder()
                .imageId(UUID.randomUUID())
                .filePath(urlCover)
                .displayOrder(0)
                .build();

            final var result = mapper.toDto(entity, List.of(img1, img2));

            assertThat(result).isNotNull();
            assertThat(result.name()).isEqualTo(petName);
            assertThat(result.coverImageUrl()).isEqualTo(urlCover);
            assertThat(result.images()).hasSize(2);
            assertThat(result.images().getFirst().url()).isEqualTo(urlCover);
        }

        @Test
        @DisplayName("Should return null when entity is null")
        void should_return_null_when_null() {
            assertThat(mapper.toDto(null)).isNull();
            assertThat(mapper.toDto(null, null)).isNull();
        }

        @Test
        @DisplayName("Should handle single-parameter toDto and null or empty image collections")
        void should_handle_single_parameter_to_dto_and_empty_images() {
            final var profileId = UUID.randomUUID();
            final var profile = ProfileEntity.builder().profileId(profileId).build();
            final var dto = CreatePetV2Dto.builder()
                .name(secure().nextAlphabetic(8))
                .species(PetSpecies.DOG)
                .build();

            final var entity = mapper.toEntity(dto, profile);

            final var resultSingle = mapper.toDto(entity);
            assertThat(resultSingle).isNotNull();
            assertThat(resultSingle.coverImageUrl()).isNull();
            assertThat(resultSingle.images()).isEmpty();

            final var resultWithNullImages = mapper.toDto(entity, null);
            assertThat(resultWithNullImages).isNotNull();
            assertThat(resultWithNullImages.coverImageUrl()).isNull();
            assertThat(resultWithNullImages.images()).isEmpty();

            final var resultWithEmptyImages = mapper.toDto(entity, List.of());
            assertThat(resultWithEmptyImages).isNotNull();
            assertThat(resultWithEmptyImages.coverImageUrl()).isNull();
            assertThat(resultWithEmptyImages.images()).isEmpty();
        }
    }

    @Nested
    @DisplayName("extractCoverImageUrl")
    class ExtractCoverImageUrlTests {

        @Test
        @DisplayName("Should return null when images list is null or empty")
        void should_return_null_when_images_null_or_empty() {
            assertThat(mapper.extractCoverImageUrl(null)).isNull();
            assertThat(mapper.extractCoverImageUrl(List.of())).isNull();
        }

        @Test
        @DisplayName("Should handle images with null display order gracefully")
        void should_handle_images_with_null_display_order() {
            final var url1 = "https://cdn.buddy.com/" + secure().nextAlphabetic(10) + ".jpg";
            final var url2 = "https://cdn.buddy.com/" + secure().nextAlphabetic(10) + ".jpg";

            final var img1 = ImageEntity.builder()
                .filePath(url1)
                .displayOrder(null)
                .build();
            final var img2 = ImageEntity.builder()
                .filePath(url2)
                .displayOrder(1)
                .build();

            assertThat(mapper.extractCoverImageUrl(List.of(img1, img2)))
                .isEqualTo(url2);
        }
    }

    @Nested
    @DisplayName("mapImages")
    class MapImagesTests {

        @Test
        @DisplayName("Should return empty list when images list is null or empty")
        void should_return_empty_list_when_images_null_or_empty() {
            assertThat(mapper.mapImages(null)).isEmpty();
            assertThat(mapper.mapImages(List.of())).isEmpty();
        }

        @Test
        @DisplayName("Should sort images and handle null display orders")
        void should_sort_images_and_handle_null_display_orders() {
            final var id1 = UUID.randomUUID();
            final var id2 = UUID.randomUUID();
            final var url1 = "https://cdn.buddy.com/" + secure().nextAlphabetic(10) + ".jpg";
            final var url2 = "https://cdn.buddy.com/" + secure().nextAlphabetic(10) + ".jpg";

            final var img1 = ImageEntity.builder()
                .imageId(id1)
                .filePath(url1)
                .displayOrder(null)
                .build();
            final var img2 = ImageEntity.builder()
                .imageId(id2)
                .filePath(url2)
                .displayOrder(1)
                .build();

            final var result = mapper.mapImages(List.of(img1, img2));

            assertThat(result).hasSize(2);
            assertThat(result.get(0).id()).isEqualTo(id2);
            assertThat(result.get(1).id()).isEqualTo(id1);
        }
    }
}
