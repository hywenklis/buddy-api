package com.buddy.api.units.commons.configurations.properties;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.buddy.api.commons.configurations.properties.BuddySecurityProperties;
import com.buddy.api.units.UnitTestAbstract;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class BuddySecurityPropertiesTest extends UnitTestAbstract {

    private Validator validator;

    private static final String KEY_16_BYTES = "1234567890123456";
    private static final String KEY_24_BYTES = "123456789012345678901234";
    private static final String KEY_32_BYTES = "12345678901234567890123456789012";

    @BeforeEach
    void setUp() {
        validator = Validation.buildDefaultValidatorFactory().getValidator();
    }

    @ParameterizedTest
    @ValueSource(strings = {KEY_16_BYTES, KEY_24_BYTES, KEY_32_BYTES})
    @DisplayName("Should instantiate successfully "
        + "when encryption key length is valid (16, 24, or 32 bytes)")
    void constructor_withValidKeyLength_shouldSucceed(String validKey) {
        assertThatCode(() -> new BuddySecurityProperties(
            validKey, "AES/GCM/NoPadding", 128, 12
        )).doesNotThrowAnyException();
    }

    @ParameterizedTest
    @ValueSource(strings = {
        "short",
        "123456789012345",
        "12345678901234567",
        "very_long_key_that_exceeds_32_bytes_limit_for_sure_123"
    })
    @DisplayName("Should throw IllegalArgumentException when encryption key length is invalid")
    void constructor_withInvalidKeyLength_shouldThrowException(String invalidKey) {
        assertThatThrownBy(() -> new BuddySecurityProperties(
            invalidKey, "AES/GCM/NoPadding", 128, 12
        ))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Invalid AES key length");
    }

    @Test
    @DisplayName("Should detect constraint violations when fields are null or blank")
    void validation_withNullOrBlankFields_shouldReturnViolations() {
        final var properties =
            new BuddySecurityProperties(null, "", null, null);

        Set<ConstraintViolation<BuddySecurityProperties>> violations =
            validator.validate(properties);

        assertThat(violations).hasSize(4);

        assertThat(violations).anyMatch(v ->
            v.getPropertyPath().toString().equals("encryptionKey") &&
                v.getMessage().contains("Encryption key cannot be empty"));

        assertThat(violations).anyMatch(v ->
            v.getPropertyPath().toString().equals("algorithm") &&
                v.getMessage().contains("Algorithm cannot be empty"));

        assertThat(violations).anyMatch(v ->
            v.getPropertyPath().toString().equals("tagLength") &&
                v.getMessage().contains("Tag length cannot be null"));

        assertThat(violations).anyMatch(v ->
            v.getPropertyPath().toString().equals("ivLength") &&
                v.getMessage().contains("IV length cannot be null"));
    }

    @Test
    @DisplayName("Should detect constraint violations when numeric values are incorrect")
    void validation_withInvalidNumbers_shouldReturnViolations() {
        var properties = new BuddySecurityProperties(
            KEY_32_BYTES,
            "AES",
            96,
            16
        );

        Set<ConstraintViolation<BuddySecurityProperties>> violations =
            validator.validate(properties);

        assertThat(violations).hasSize(2);

        assertThat(violations).anyMatch(v ->
            v.getPropertyPath().toString().equals("tagLength") &&
                v.getMessage().contains("must be 128 bits"));

        assertThat(violations).anyMatch(v ->
            v.getPropertyPath().toString().equals("ivLength") &&
                v.getMessage().contains("must be 12 bytes"));
    }

    @Test
    @DisplayName("Should pass validation when all fields are correct")
    void validation_happyPath() {
        var properties = new BuddySecurityProperties(
            KEY_32_BYTES,
            "AES/GCM/NoPadding",
            128,
            12
        );

        Set<ConstraintViolation<BuddySecurityProperties>> violations =
            validator.validate(properties);

        assertThat(violations).isEmpty();
    }
}
