package com.buddy.api.web.accounts.requests;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record ResetPasswordRequest(
    @Schema(description = "Reset token sent via email", example = "eyJhbGciOiJIUzI1NiJ9...")
    @NotBlank(message = "Reset token is mandatory")
    String token,

    @Schema(description = "New password", example = "newPassword123")
    @NotBlank(message = "New password is mandatory")
    @Size(
        message = "New password must have between 6 and 16 characters",
        min = 6,
        max = 16
    )
    @Pattern(
        regexp = PasswordPolicy.STRONG_PASSWORD_REGEX,
        message = PasswordPolicy.STRONG_PASSWORD_MESSAGE
    )
    String newPassword
) {
}
