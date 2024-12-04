package com.buddy.api.domains.account.services;

import com.buddy.api.domains.account.dtos.AccountDto;

public interface CreateAccount {
    void create(AccountDto accountDto);
}
