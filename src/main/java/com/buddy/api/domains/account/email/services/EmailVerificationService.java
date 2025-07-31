package com.buddy.api.domains.account.email.services;

import com.buddy.api.domains.account.dtos.AccountDto;

public interface EmailVerificationService {
    void requestEmail(AccountDto account);

    void confirmEmail(String token, AccountDto accountDto);
}
