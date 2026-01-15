package com.buddy.api.commons.configurations.properties;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.nio.charset.StandardCharsets;
import java.util.Set;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "buddy.security")
public record BuddySecurityProperties(
    @NotBlank(message = "Encryption key cannot be empty")
    String encryptionKey,

    @NotBlank(message = "Algorithm cannot be empty")
    String algorithm,

    @NotNull(message = "Tag length cannot be null")
    @Min(value = 128, message = "Tag length must be 128 bits")
    @Max(value = 128, message = "Tag length must be 128 bits")
    Integer tagLength,

    @NotNull(message = "IV length cannot be null")
    @Min(value = 12, message = "IV length must be 12 bytes")
    @Max(value = 12, message = "IV length must be 12 bytes")
    Integer ivLength
) {
    private static final Set<Integer> VALID_AES_KEY_SIZES = Set.of(16, 24, 32);

    public BuddySecurityProperties {
        if (encryptionKey != null && !isValidKeyLength(encryptionKey)) {
            throw new IllegalArgumentException(
                "Invalid AES key length. Must be exactly 16, 24, or 32 bytes (UTF-8)"
            );
        }
    }

    private boolean isValidKeyLength(final String key) {
        int length = key.getBytes(StandardCharsets.UTF_8).length;
        return VALID_AES_KEY_SIZES.contains(length);
    }
}
