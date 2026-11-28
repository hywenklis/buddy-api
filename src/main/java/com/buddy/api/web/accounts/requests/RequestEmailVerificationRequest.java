package com.buddy.api.web.accounts.requests;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Builder;

@Builder
public record RequestEmailVerificationRequest(
    @Schema(description = "Account email", example = "account@email.com")
    @NotBlank(message = "Account email is mandatory")
    @Email(message = "Account email must be a valid email address")
    String email
) { }
