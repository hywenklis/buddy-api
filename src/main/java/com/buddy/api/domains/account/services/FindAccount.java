package com.buddy.api.domains.account.services;

import com.buddy.api.domains.account.dtos.AccountDto;
import java.util.UUID;

public interface FindAccount {
    Boolean existsById(UUID accountId);

    AccountDto findByEmail(String email);

    AccountDto findAccountForAuthentication(String email);
}
