package com.buddy.api.web.accounts.requests;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Builder;

@Builder
public record ConfirmEmailRequest(
    @NotBlank(message = "token is required")
    @Pattern(regexp = "^[0-9a-fA-F\\-]{36}$", message = "token must be a valid UUID format")
    String token
) { }
