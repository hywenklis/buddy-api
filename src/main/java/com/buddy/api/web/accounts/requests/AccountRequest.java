package com.buddy.api.web.accounts.requests;

import com.buddy.api.domains.account.dtos.AccountDto;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Builder;

@Builder
public record AccountRequest(
    @Schema(description = "Account email", example = "account@email.com")
    @NotBlank(message = "Account email is mandatory")
    @Email(message = "Account email must be a valid email address")
    String email,

    @Schema(description = "Account phoneNumber", example = "000000000")
    @Pattern(regexp = "^\\d+$", message = "Account phone number must contain only digits")
    @Size(
        message = "Account phone number must have between 4 and 20 digits",
        min = 4,
        max = 20
    )
    String phoneNumber,

    @Schema(description = "Account password", example = "password")
    @NotBlank(message = "Account password is mandatory")
    @Size(
        message = "Account password must have between 6 and 16 characters",
        min = 6,
        max = 16
    )
    String password,

    @Schema(description = "Account acceptance of the terms of user", example = "true")
    @NotNull(message = "Account terms of user consent information is mandatory")
    Boolean termsOfUserConsent
) {
    public AccountDto toAccountDto() {
        return new AccountDto(email, phoneNumber, password, termsOfUserConsent);
    }
}
