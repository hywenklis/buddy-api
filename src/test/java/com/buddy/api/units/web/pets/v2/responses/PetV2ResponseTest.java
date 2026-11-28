package com.buddy.api.units.web.pets.v2.responses;

import static org.apache.commons.lang3.RandomStringUtils.secure;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.buddy.api.domains.pet.enums.PetGender;
import com.buddy.api.domains.pet.enums.PetSpecies;
import com.buddy.api.web.pets.v2.responses.PetV2ImageResponse;
import com.buddy.api.web.pets.v2.responses.PetV2Response;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

@DisplayName("PetV2Response — Unit Tests")
class PetV2ResponseTest {

    @Nested
    @DisplayName("Compact Constructor & Defensive Copying")
    class CompactConstructorTests {

        @Test
        @DisplayName("Should initialize empty list when images parameter is null")
        void should_initialize_empty_list_when_images_null() {
            final var petName = secure().nextAlphabetic(8);
            final var response = PetV2Response.builder()
                .id(UUID.randomUUID())
                .name(petName)
                .species(PetSpecies.DOG)
                .gender(PetGender.MALE)
                .images(null)
                .build();

            assertThat(response.images()).isNotNull().isEmpty();
        }

        @Test
        @DisplayName("Should preserve provided images and guarantee defensive copying")
        void should_preserve_images_and_guarantee_immutability() {
            final var imageId = UUID.randomUUID();
            final var petName = secure().nextAlphabetic(8);
            final var imageUrl = "https://cdn.buddy.com/" + secure().nextAlphabetic(10) + ".jpg";

            final var image = PetV2ImageResponse.builder()
                .id(imageId)
                .url(imageUrl)
                .displayOrder(0)
                .build();

            final var mutableList = new ArrayList<PetV2ImageResponse>();
            mutableList.add(image);

            final var response = PetV2Response.builder()
                .id(UUID.randomUUID())
                .name(petName)
                .images(mutableList)
                .build();

            assertThat(response.images()).hasSize(1).containsExactly(image);

            mutableList.clear();
            assertThat(response.images()).hasSize(1);

            final var images = response.images();
            assertThatThrownBy(() -> images.add(image))
                .isInstanceOf(UnsupportedOperationException.class);
        }

        @Test
        @DisplayName("Should construct full record with all attributes")
        void should_construct_full_record() {
            final var petId = UUID.randomUUID();
            final var guardianId = UUID.randomUUID();
            final var petName = secure().nextAlphabetic(8);
            final var description = secure().nextAlphabetic(20);
            final var coverUrl = "https://cdn.buddy.com/" + secure().nextAlphabetic(10) + ".jpg";
            final var now = LocalDateTime.now();
            final var today = LocalDate.now();

            final var response = new PetV2Response(
                petId,
                guardianId,
                petName,
                PetSpecies.CAT,
                PetGender.FEMALE,
                3,
                today,
                BigDecimal.valueOf(25.0),
                BigDecimal.valueOf(4.5),
                true,
                true,
                description,
                coverUrl,
                null,
                now,
                now
            );

            assertThat(response.id()).isEqualTo(petId);
            assertThat(response.guardianProfileId()).isEqualTo(guardianId);
            assertThat(response.name()).isEqualTo(petName);
            assertThat(response.species()).isEqualTo(PetSpecies.CAT);
            assertThat(response.gender()).isEqualTo(PetGender.FEMALE);
            assertThat(response.approximateAge()).isEqualTo(3);
            assertThat(response.ageReportDate()).isEqualTo(today);
            assertThat(response.size()).isEqualTo(BigDecimal.valueOf(25.0));
            assertThat(response.weight()).isEqualTo(BigDecimal.valueOf(4.5));
            assertThat(response.isNeutered()).isTrue();
            assertThat(response.isForAdoption()).isTrue();
            assertThat(response.description()).isEqualTo(description);
            assertThat(response.coverImageUrl()).isEqualTo(coverUrl);
            assertThat(response.images()).isEmpty();
            assertThat(response.creationDate()).isEqualTo(now);
            assertThat(response.updatedDate()).isEqualTo(now);
        }
    }
}
