package com.buddy.api.commons.configurations.properties;

import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties(prefix = "notification.api")
@Builder
public record NotificationApiProperties(
    @NotBlank String baseUrl,
    @NotBlank String user
) {}
