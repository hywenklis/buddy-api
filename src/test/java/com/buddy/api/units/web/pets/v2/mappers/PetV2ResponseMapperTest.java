package com.buddy.api.units.web.pets.v2.mappers;

import static org.assertj.core.api.Assertions.assertThat;

import com.buddy.api.domains.pet.dtos.v2.PetV2Dto;
import com.buddy.api.domains.pet.dtos.v2.PetV2ImageDto;
import com.buddy.api.domains.pet.enums.PetGender;
import com.buddy.api.domains.pet.enums.PetSpecies;
import com.buddy.api.web.pets.v2.mappers.PetV2ResponseMapper;
import com.buddy.api.web.pets.v2.mappers.PetV2ResponseMapperImpl;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

@DisplayName("PetV2ResponseMapper — Unit Tests")
class PetV2ResponseMapperTest {

    private PetV2ResponseMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = new PetV2ResponseMapperImpl();
    }

    @Nested
    @DisplayName("toResponse")
    class ToResponseTests {

        @Test
        @DisplayName("Should map PetV2Dto to PetV2Response with images")
        void should_map_to_detail_response() {
            final var petId = UUID.randomUUID();
            final var profileId = UUID.randomUUID();
            final var imageId = UUID.randomUUID();
            final var now = LocalDateTime.now();

            final var imageDto = PetV2ImageDto.builder()
                .id(imageId)
                .url("http://cdn.buddy.com/photo.jpg")
                .displayOrder(0)
                .build();

            final var dto = PetV2Dto.builder()
                .id(petId)
                .guardianProfileId(profileId)
                .name("Bella")
                .species(PetSpecies.CAT)
                .gender(PetGender.FEMALE)
                .approximateAge(1)
                .ageReportDate(LocalDate.now())
                .size(BigDecimal.valueOf(25))
                .weight(BigDecimal.valueOf(3))
                .isNeutered(false)
                .isForAdoption(true)
                .description("Playful kitten")
                .coverImageUrl("http://cdn.buddy.com/photo.jpg")
                .images(List.of(imageDto))
                .creationDate(now)
                .updatedDate(now)
                .build();

            final var response = mapper.toResponse(dto);

            assertThat(response).isNotNull();
            assertThat(response.id()).isEqualTo(petId);
            assertThat(response.guardianProfileId()).isEqualTo(profileId);
            assertThat(response.name()).isEqualTo("Bella");
            assertThat(response.species()).isEqualTo(PetSpecies.CAT);
            assertThat(response.gender()).isEqualTo(PetGender.FEMALE);
            assertThat(response.coverImageUrl()).isEqualTo("http://cdn.buddy.com/photo.jpg");
            assertThat(response.images()).hasSize(1);
            assertThat(response.images().get(0).id()).isEqualTo(imageId);
        }

        @Test
        @DisplayName("Should return null when dto is null")
        void should_return_null_when_null() {
            assertThat(mapper.toResponse(null)).isNull();
            assertThat(mapper.toSummaryResponse(null)).isNull();
            assertThat(mapper.toImageResponse(null)).isNull();
        }
    }

    @Nested
    @DisplayName("toSummaryResponse")
    class ToSummaryResponseTests {

        @Test
        @DisplayName("Should map PetV2Dto to PetV2SummaryResponse")
        void should_map_to_summary_response() {
            final var petId = UUID.randomUUID();
            final var profileId = UUID.randomUUID();
            final var now = LocalDateTime.now();

            final var dto = PetV2Dto.builder()
                .id(petId)
                .guardianProfileId(profileId)
                .name("Buddy")
                .species(PetSpecies.DOG)
                .gender(PetGender.MALE)
                .approximateAge(4)
                .size(BigDecimal.valueOf(50))
                .weight(BigDecimal.valueOf(20))
                .isNeutered(true)
                .isForAdoption(true)
                .coverImageUrl("http://cdn.buddy.com/cover.jpg")
                .creationDate(now)
                .build();

            final var summary = mapper.toSummaryResponse(dto);

            assertThat(summary).isNotNull();
            assertThat(summary.id()).isEqualTo(petId);
            assertThat(summary.guardianProfileId()).isEqualTo(profileId);
            assertThat(summary.name()).isEqualTo("Buddy");
            assertThat(summary.coverImageUrl()).isEqualTo("http://cdn.buddy.com/cover.jpg");
            assertThat(summary.creationDate()).isEqualTo(now);
        }
    }
}
