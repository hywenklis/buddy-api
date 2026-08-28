package com.buddy.api.units.web.pets.v2.requests;

import static org.assertj.core.api.Assertions.assertThat;

import com.buddy.api.web.pets.v2.requests.PetV2SearchCriteriaRequest;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import java.math.BigDecimal;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

@DisplayName("PetV2SearchCriteriaRequest — Unit Tests")
class PetV2SearchCriteriaRequestTest {

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
        @DisplayName("Should pass validation with valid ranges and positive values")
        void should_pass_validation_with_valid_ranges() {
            final var request = PetV2SearchCriteriaRequest.builder()
                .minSize(BigDecimal.valueOf(10))
                .maxSize(BigDecimal.valueOf(50))
                .minWeight(BigDecimal.valueOf(2.5))
                .maxWeight(BigDecimal.valueOf(20.0))
                .minAge(1)
                .maxAge(10)
                .build();

            final var violations = validator.validate(request);
            assertThat(violations).isEmpty();
            assertThat(request.isSizeRangeValid()).isTrue();
            assertThat(request.isWeightRangeValid()).isTrue();
            assertThat(request.isAgeRangeValid()).isTrue();
        }

        @Test
        @DisplayName("Should pass validation when all fields are null")
        void should_pass_validation_when_null() {
            final var request = PetV2SearchCriteriaRequest.builder().build();
            final var violations = validator.validate(request);
            assertThat(violations).isEmpty();
            assertThat(request.isSizeRangeValid()).isTrue();
            assertThat(request.isWeightRangeValid()).isTrue();
            assertThat(request.isAgeRangeValid()).isTrue();
        }
    }

    @Nested
    @DisplayName("Invalid Bounds Tests")
    class InvalidBoundsTests {

        @Test
        @DisplayName("Should fail validation when values are negative")
        void should_fail_when_negative() {
            final var request = PetV2SearchCriteriaRequest.builder()
                .minSize(BigDecimal.valueOf(-1))
                .maxWeight(BigDecimal.valueOf(-5))
                .minAge(-2)
                .build();

            final var violations = validator.validate(request);
            assertThat(violations).hasSize(3);
        }

        @Test
        @DisplayName("Should fail validation when min is greater than max")
        void should_fail_when_min_greater_than_max() {
            final var request = PetV2SearchCriteriaRequest.builder()
                .minSize(BigDecimal.valueOf(50))
                .maxSize(BigDecimal.valueOf(10))
                .minWeight(BigDecimal.valueOf(30.0))
                .maxWeight(BigDecimal.valueOf(5.0))
                .minAge(10)
                .maxAge(2)
                .build();

            final var violations = validator.validate(request);
            assertThat(violations).hasSize(3);
            assertThat(request.isSizeRangeValid()).isFalse();
            assertThat(request.isWeightRangeValid()).isFalse();
            assertThat(request.isAgeRangeValid()).isFalse();
        }
    }
}
