package com.buddy.api.domains.account.password.dtos;

public record ResetPasswordDto(
    String token,
    String newPassword
) {
}
