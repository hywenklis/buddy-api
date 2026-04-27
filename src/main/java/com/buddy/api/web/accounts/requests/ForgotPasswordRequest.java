package com.buddy.api.web.accounts.requests;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Builder;

@Builder
public record ForgotPasswordRequest(
    @Schema(description = "Account email for password recovery", example = "user@email.com")
    @NotBlank(message = "Email is mandatory")
    @Email(message = "Email must be a valid email address")
    String email
) { }
