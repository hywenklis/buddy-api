package com.buddy.api.web.accounts.requests;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Builder;

@Builder
public record AccountRequest(
    @NotBlank(message = "Account email is mandatory")
    @Email(message = "Account email must be a valid email address")
    String email,
    String phoneNumber,
    @NotBlank(message = "Account password is mandatory")
    String password,
    Boolean termsOfUserConsent
) {
}
