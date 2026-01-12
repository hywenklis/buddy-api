package com.buddy.api.commons.configurations.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "buddy.security")
public record BuddySecurityProperties(
    String encryptionKey,
    String algorithm,
    Integer tagLength,
    Integer ivLength
) {}