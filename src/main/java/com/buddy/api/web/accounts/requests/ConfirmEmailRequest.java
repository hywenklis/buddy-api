package com.buddy.api.web.accounts.requests;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Builder;

@Builder
public record ConfirmEmailRequest(
    @NotBlank(message = "token is required")
    @Size(min = 36, max = 36, message = "token must be 36 characters long")
    String token
) { }
