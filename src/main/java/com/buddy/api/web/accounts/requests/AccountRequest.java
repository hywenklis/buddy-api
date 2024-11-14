package com.buddy.api.web.accounts.requests;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Builder;

@Builder
public record AccountRequest(
    @NotBlank(message = "Account email is mandatory")
    @Email(message = "Account email must be a valid email address")
    String email,
    String phoneNumber,
    @NotBlank(message = "Account password is mandatory")
    @Size(
        message = "Account password must have between 6 and 16 characters",
        min = 6,
        max = 16
    )
    String password,
    Boolean termsOfUserConsent
) {
}
