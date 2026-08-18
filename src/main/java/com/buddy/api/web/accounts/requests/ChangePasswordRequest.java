package com.buddy.api.web.accounts.requests;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ChangePasswordRequest(
    @Schema(description = "Current password", example = "oldPassword123")
    @NotBlank(message = "currentPassword is required")
    String currentPassword,

    @Schema(description = "New password", example = "newPassword123")
    @NotBlank(message = "newPassword is required")
    @Size(
        message = "New password must have between 6 and 16 characters",
        min = 6,
        max = 16
    )
    String newPassword
) { }
