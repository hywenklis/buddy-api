package com.buddy.api.units.web.pets.v2.mappers;

import static org.assertj.core.api.Assertions.assertThat;

import com.buddy.api.domains.pet.enums.PetGender;
import com.buddy.api.domains.pet.enums.PetSpecies;
import com.buddy.api.web.pets.v2.mappers.PetV2RequestMapper;
import com.buddy.api.web.pets.v2.mappers.PetV2RequestMapperImpl;
import com.buddy.api.web.pets.v2.requests.CreatePetV2Request;
import com.buddy.api.web.pets.v2.requests.PetV2SearchCriteriaRequest;
import com.buddy.api.web.pets.v2.requests.UpdatePetV2Request;
import java.math.BigDecimal;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

@DisplayName("PetV2RequestMapper — Unit Tests")
class PetV2RequestMapperTest {

    private PetV2RequestMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = new PetV2RequestMapperImpl();
    }

    @Nested
    @DisplayName("toCreateDto")
    class ToCreateDtoTests {

        @Test
        @DisplayName("Should map CreatePetV2Request to CreatePetV2Dto")
        void should_map_create_request() {
            final var accountId = UUID.randomUUID();
            final var request = CreatePetV2Request.builder()
                .name("Max")
                .species(PetSpecies.DOG)
                .gender(PetGender.MALE)
                .approximateAge(2)
                .size(BigDecimal.valueOf(40))
                .weight(BigDecimal.valueOf(10))
                .isNeutered(true)
                .isForAdoption(true)
                .description("Good boy")
                .build();

            final var dto = mapper.toCreateDto(request, accountId);

            assertThat(dto).isNotNull();
            assertThat(dto.accountId()).isEqualTo(accountId);
            assertThat(dto.name()).isEqualTo("Max");
            assertThat(dto.species()).isEqualTo(PetSpecies.DOG);
            assertThat(dto.gender()).isEqualTo(PetGender.MALE);
            assertThat(dto.approximateAge()).isEqualTo(2);
            assertThat(dto.size()).isEqualTo(BigDecimal.valueOf(40));
            assertThat(dto.weight()).isEqualTo(BigDecimal.valueOf(10));
            assertThat(dto.isNeutered()).isTrue();
            assertThat(dto.isForAdoption()).isTrue();
            assertThat(dto.description()).isEqualTo("Good boy");
        }

        @Test
        @DisplayName("Should return null when request is null")
        void should_return_null_when_null() {
            assertThat(mapper.toCreateDto(null, null)).isNull();
        }
    }

    @Nested
    @DisplayName("toUpdateDto")
    class ToUpdateDtoTests {

        @Test
        @DisplayName("Should map UpdatePetV2Request to UpdatePetV2Dto")
        void should_map_update_request() {
            final var petId = UUID.randomUUID();
            final var request = UpdatePetV2Request.builder()
                .name("Updated")
                .species(PetSpecies.CAT)
                .gender(PetGender.FEMALE)
                .build();

            final var dto = mapper.toUpdateDto(petId, request);

            assertThat(dto).isNotNull();
            assertThat(dto.id()).isEqualTo(petId);
            assertThat(dto.name()).isEqualTo("Updated");
            assertThat(dto.species()).isEqualTo(PetSpecies.CAT);
            assertThat(dto.gender()).isEqualTo(PetGender.FEMALE);
        }

        @Test
        @DisplayName("Should return null when request and id are null")
        void should_return_null_when_null() {
            assertThat(mapper.toUpdateDto(null, null)).isNull();
        }
    }

    @Nested
    @DisplayName("toSearchCriteriaDto")
    class ToSearchCriteriaDtoTests {

        @Test
        @DisplayName("Should map PetV2SearchCriteriaRequest to PetV2SearchCriteriaDto")
        void should_map_search_criteria() {
            final var request = PetV2SearchCriteriaRequest.builder()
                .name("Thor")
                .species(PetSpecies.DOG)
                .gender(PetGender.MALE)
                .isNeutered(true)
                .isForAdoption(true)
                .minAge(1)
                .maxAge(5)
                .build();

            final var dto = mapper.toSearchCriteriaDto(request);

            assertThat(dto).isNotNull();
            assertThat(dto.name()).isEqualTo("Thor");
            assertThat(dto.species()).isEqualTo(PetSpecies.DOG);
            assertThat(dto.gender()).isEqualTo(PetGender.MALE);
            assertThat(dto.isNeutered()).isTrue();
            assertThat(dto.isForAdoption()).isTrue();
            assertThat(dto.minAge()).isEqualTo(1);
            assertThat(dto.maxAge()).isEqualTo(5);
        }

        @Test
        @DisplayName("Should return null when search criteria request is null")
        void should_return_null_when_null() {
            assertThat(mapper.toSearchCriteriaDto(null)).isNull();
        }
    }
}
