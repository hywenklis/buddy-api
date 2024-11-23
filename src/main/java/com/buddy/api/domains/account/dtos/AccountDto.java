package com.buddy.api.domains.account.dtos;

import com.buddy.api.domains.account.entities.AccountEntity;
import com.buddy.api.domains.valueobjects.EmailAddress;
import lombok.Builder;

@Builder
public record AccountDto(EmailAddress email,
                         String phoneNumber,
                         String password,
                         Boolean termsOfUserConsent) {
    // Caso o mapeamento cresça ou fique mais complexo
    // Refatorar para utilizar um mapper próprio através do MapStruct
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
