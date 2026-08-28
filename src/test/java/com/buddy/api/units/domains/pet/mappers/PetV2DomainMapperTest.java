package com.buddy.api.units.domains.pet.mappers;

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
            final var profile = ProfileEntity.builder().profileId(UUID.randomUUID()).build();
            final var dto = CreatePetV2Dto.builder()
                .guardianProfileId(profile.getProfileId())
                .name("Rex")
                .species(PetSpecies.DOG)
                .gender(PetGender.MALE)
                .approximateAge(3)
                .size(BigDecimal.valueOf(45.5))
                .weight(BigDecimal.valueOf(12.0))
                .isNeutered(true)
                .isForAdoption(true)
                .description("Friendly dog")
                .build();

            final var entity = mapper.toEntity(dto, profile);

            assertThat(entity).isNotNull();
            assertThat(entity.getName()).isEqualTo("Rex");
            assertThat(entity.getSpecies()).isEqualTo(PetSpecies.DOG);
            assertThat(entity.getGender()).isEqualTo(PetGender.MALE);
            assertThat(entity.getApproximateAge()).isEqualTo(3);
            assertThat(entity.getAgeReportDate()).isEqualTo(LocalDate.now());
            assertThat(entity.getSize()).isEqualTo(BigDecimal.valueOf(45.5));
            assertThat(entity.getWeight()).isEqualTo(BigDecimal.valueOf(12.0));
            assertThat(entity.getIsNeutered()).isTrue();
            assertThat(entity.getIsForAdoption()).isTrue();
            assertThat(entity.getDescription()).isEqualTo("Friendly dog");
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
            final var profileId = UUID.randomUUID();
            final var profile = ProfileEntity.builder().profileId(profileId).build();
            final var dto = CreatePetV2Dto.builder()
                .name("Luna")
                .species(PetSpecies.CAT)
                .gender(PetGender.FEMALE)
                .build();

            final var entity = mapper.toEntity(dto, profile);

            final var img1 = ImageEntity.builder()
                .imageId(UUID.randomUUID())
                .filePath("http://cdn.buddy.com/luna-2.jpg")
                .displayOrder(2)
                .build();

            final var img2 = ImageEntity.builder()
                .imageId(UUID.randomUUID())
                .filePath("http://cdn.buddy.com/luna-0.jpg")
                .displayOrder(0)
                .build();

            final var result = mapper.toDto(entity, List.of(img1, img2));

            assertThat(result).isNotNull();
            assertThat(result.name()).isEqualTo("Luna");
            assertThat(result.coverImageUrl()).isEqualTo("http://cdn.buddy.com/luna-0.jpg");
            assertThat(result.images()).hasSize(2);
            assertThat(result.images().get(0).url()).isEqualTo("http://cdn.buddy.com/luna-0.jpg");
        }

        @Test
        @DisplayName("Should return null when entity is null")
        void should_return_null_when_null() {
            assertThat(mapper.toDto(null)).isNull();
            assertThat(mapper.toDto(null, null)).isNull();
        }
    }
}
