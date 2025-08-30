package com.buddy.api.domains.account.email.services;

import com.buddy.api.domains.account.dtos.AccountDto;
import java.util.UUID;

public interface AccountValidator {
    void validateAccountNotVerified(final AccountDto account);

    void validateTokenMatchesAccount(final AccountDto account,
                                     final String emailFromToken,
                                     final UUID accountId);
}
