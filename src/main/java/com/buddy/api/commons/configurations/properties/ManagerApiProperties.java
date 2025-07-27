package com.buddy.api.commons.configurations.properties;

import jakarta.validation.constraints.NotBlank;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties(prefix = "manager.api")
public record ManagerApiProperties(
    @NotBlank(message = "contentType is required") String contentType,
    @NotBlank(message = "userAgent is required") String userAgent,
    @NotBlank(message = "ipAddress is required") String ipAddress,
    @NotBlank(message = "username is required") String username,
    @NotBlank(message = "password is required") String password
) {}
