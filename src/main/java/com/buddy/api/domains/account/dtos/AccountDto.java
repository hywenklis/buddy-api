package com.buddy.api.domains.account.dtos;

import com.buddy.api.domains.account.entities.AccountEntity;

public record AccountDto(String email,
                         String phoneNumber,
                         String password,
                         Boolean termsOfUserConsent) {
    public AccountEntity toAccountEntity() {
        return new AccountEntity(
            null,
            email,
            phoneNumber,
            password,
            termsOfUserConsent,
            false,
            false,
            false,
            null,
            null,
            null
        );
    }
}
