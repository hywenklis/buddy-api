package com.buddy.api.commons.configurations.properties;

import jakarta.validation.constraints.NotBlank;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties(prefix = "notification.api")
public record NotificationApiProperties(
    @NotBlank String baseUrl,
    @NotBlank String xUser
) {}
