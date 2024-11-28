package com.buddy.api.domains.account.dtos;

import com.buddy.api.domains.valueobjects.EmailAddress;
import lombok.Builder;

@Builder
public record AccountDto(EmailAddress email,
                         String phoneNumber,
                         String password,
                         Boolean termsOfUserConsent) {
}
