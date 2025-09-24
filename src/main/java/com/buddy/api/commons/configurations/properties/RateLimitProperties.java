package com.buddy.api.commons.configurations.properties;

import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties(prefix = "buddy.rate.limit")
@Builder
public record RateLimitProperties(
    @NotNull(message = "maxAttempts is required") Integer maxAttempts,
    @NotNull(message = "windowMinutes is required") Integer windowMinutes
) { }
