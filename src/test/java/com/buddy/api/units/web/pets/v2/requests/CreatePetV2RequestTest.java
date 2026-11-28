package com.buddy.api.units.web.pets.v2.requests;

import static org.assertj.core.api.Assertions.assertThat;

import com.buddy.api.domains.pet.enums.PetGender;
import com.buddy.api.domains.pet.enums.PetSpecies;
import com.buddy.api.web.pets.v2.requests.CreatePetV2Request;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import java.math.BigDecimal;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

@DisplayName("CreatePetV2Request — Unit Tests")
class CreatePetV2RequestTest {

    private static ValidatorFactory validatorFactory;
    private static Validator validator;

    @BeforeAll
    static void setUpValidator() {
        validatorFactory = Validation.buildDefaultValidatorFactory();
        validator = validatorFactory.getValidator();
    }

    @AfterAll
    static void tearDownValidator() {
        if (validatorFactory != null) {
            validatorFactory.close();
        }
    }

    @Nested
    @DisplayName("Valid Requests and Defaults")
    class ValidRequestsTests {

        @Test
        @DisplayName("Should pass validation with valid payload and preserve isForAdoption")
        void should_pass_validation_with_valid_payload() {
            final var request = CreatePetV2Request.builder()
                .name("Thor")
                .species(PetSpecies.DOG)
                .gender(PetGender.MALE)
                .approximateAge(0)
                .size(new BigDecimal("99999999.99"))
                .weight(new BigDecimal("14.20"))
                .isNeutered(true)
                .isForAdoption(false)
                .description("Friendly dog.")
                .build();

            assertThat(validator.validate(request)).isEmpty();
            assertThat(request.isForAdoption()).isFalse();
        }

        @Test
        @DisplayName("Should default isForAdoption to true when omitted and accept age 50")
        void should_default_is_for_adoption_to_true_when_null() {
            final var request = CreatePetV2Request.builder()
                .name("Mimi")
                .species(PetSpecies.CAT)
                .gender(PetGender.FEMALE)
                .approximateAge(50)
                .build();

            assertThat(validator.validate(request)).isEmpty();
            assertThat(request.isForAdoption()).isTrue();
        }
    }

    @Nested
    @DisplayName("Invalid Requests")
    class InvalidRequestsTests {

        @Test
        @DisplayName("Should fail validation when mandatory fields are missing or blank")
        void should_fail_when_mandatory_fields_missing() {
            final var request = CreatePetV2Request.builder()
                .name("   ")
                .species(null)
                .gender(null)
                .build();

            assertThat(validator.validate(request)).isNotEmpty();
        }

        @Test
        @DisplayName("Should fail validation when name length is out of bounds")
        void should_fail_when_name_length_invalid() {
            final var shortNameRequest = CreatePetV2Request.builder()
                .name("A")
                .species(PetSpecies.DOG)
                .gender(PetGender.MALE)
                .build();

            final var longNameRequest = CreatePetV2Request.builder()
                .name("a".repeat(101))
                .species(PetSpecies.DOG)
                .gender(PetGender.MALE)
                .build();

            assertThat(validator.validate(shortNameRequest)).hasSize(1);
            assertThat(validator.validate(longNameRequest)).hasSize(1);
        }

        @Test
        @DisplayName("Should fail validation when age, size, weight, or description violate bounds")
        void should_fail_when_metrics_or_description_invalid() {
            final var negativeAgeRequest = CreatePetV2Request.builder()
                .name("Thor")
                .species(PetSpecies.DOG)
                .gender(PetGender.MALE)
                .approximateAge(-1)
                .size(BigDecimal.ZERO)
                .weight(new BigDecimal("-5.00"))
                .description("d".repeat(1001))
                .build();

            final var overMaxDigitsRequest = CreatePetV2Request.builder()
                .name("Thor")
                .species(PetSpecies.DOG)
                .gender(PetGender.MALE)
                .approximateAge(51)
                .size(new BigDecimal("100000000.00"))
                .weight(new BigDecimal("10.123"))
                .build();

            assertThat(validator.validate(negativeAgeRequest)).hasSize(4);
            assertThat(validator.validate(overMaxDigitsRequest)).hasSize(3);
        }
    }
}
