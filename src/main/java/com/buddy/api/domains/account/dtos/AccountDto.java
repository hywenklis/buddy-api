package com.buddy.api.domains.account.dtos;

import com.buddy.api.domains.valueobjects.EmailAddress;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.Builder;

@Builder
public record AccountDto(UUID accountId,
                         EmailAddress email,
                         String phoneNumber,
                         String password,
                         Boolean termsOfUserConsent,
                         Boolean isDeleted,
                         Boolean isBlocked,
                         Boolean isVerified,
                         LocalDateTime lastLogin) {
}
