package com.buddy.api.web.authentication.requests;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Builder;

@Builder
public record AuthRequest(
    @Schema(description = "Account email", example = "account@email.com")
    @NotBlank(message = "Account email is mandatory")
    @Size(max = 100, message = "Account email must be at most 100 characters")
    @Email(message = "Account email must be a valid email address")
    String email,

    @Schema(description = "Account password", example = "password")
    @NotBlank(message = "Account password is mandatory")
    @Size(
        message = "Account password must have between 6 and 16 characters",
        min = 6,
        max = 16
    )
    String password
) {}
