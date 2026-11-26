package com.buddy.api.units.web.pets.v2.requests;

import static org.assertj.core.api.Assertions.assertThat;

import com.buddy.api.domains.pet.enums.PetGender;
import com.buddy.api.domains.pet.enums.PetSpecies;
import com.buddy.api.web.pets.v2.requests.UpdatePetV2Request;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import java.math.BigDecimal;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

@DisplayName("UpdatePetV2Request — Unit Tests")
class UpdatePetV2RequestTest {

    private static Validator validator;

    @BeforeAll
    static void setUpValidator() {
        try (var factory = Validation.buildDefaultValidatorFactory()) {
            validator = factory.getValidator();
        }
    }

    @Nested
    @DisplayName("Valid Requests")
    class ValidRequestsTests {

        @Test
        @DisplayName("Should pass validation when all optional fields are null")
        void should_pass_validation_when_all_fields_null() {
            final var request = UpdatePetV2Request.builder().build();

            assertThat(validator.validate(request)).isEmpty();
        }

        @Test
        @DisplayName("Should pass validation with valid full payload")
        void should_pass_validation_with_valid_payload() {
            final var request = UpdatePetV2Request.builder()
                .name("Thor Updated")
                .species(PetSpecies.DOG)
                .gender(PetGender.MALE)
                .approximateAge(50)
                .size(new BigDecimal("99999999.99"))
                .weight(new BigDecimal("15.50"))
                .isNeutered(true)
                .isForAdoption(false)
                .description("Updated description.")
                .build();

            assertThat(validator.validate(request)).isEmpty();
        }
    }

    @Nested
    @DisplayName("Invalid Requests")
    class InvalidRequestsTests {

        @Test
        @DisplayName("Should fail validation when name is whitespace-only or out of bounds")
        void should_fail_when_name_blank_or_invalid_length() {
            final var blankNameRequest = UpdatePetV2Request.builder()
                .name("   ")
                .build();
            final var shortNameRequest = UpdatePetV2Request.builder()
                .name("A")
                .build();
            final var longNameRequest = UpdatePetV2Request.builder()
                .name("a".repeat(101))
                .build();

            assertThat(validator.validate(blankNameRequest)).hasSize(1);
            assertThat(validator.validate(shortNameRequest)).hasSize(1);
            assertThat(validator.validate(longNameRequest)).hasSize(1);
        }

        @Test
        @DisplayName("Should fail validation when metrics or description violate constraints")
        void should_fail_when_metrics_or_description_invalid() {
            final var negativeAndZeroRequest = UpdatePetV2Request.builder()
                .approximateAge(-1)
                .size(BigDecimal.ZERO)
                .weight(new BigDecimal("-1.00"))
                .description("d".repeat(1001))
                .build();

            final var exceededScaleAndAgeRequest = UpdatePetV2Request.builder()
                .approximateAge(51)
                .size(new BigDecimal("100000000.00"))
                .weight(new BigDecimal("12.345"))
                .build();

            assertThat(validator.validate(negativeAndZeroRequest)).hasSize(4);
            assertThat(validator.validate(exceededScaleAndAgeRequest)).hasSize(3);
        }
    }
}
