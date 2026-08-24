package com.buddy.api.domains.account.password.dtos;

import java.util.UUID;
import lombok.Builder;

@Builder
public record ChangePasswordDto(
    UUID accountId,
    String email,
    String currentPassword,
    String newPassword,
    String ipAddress,
    String userAgent
) { }
