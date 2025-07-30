package com.buddy.api.domains.account.services;

import com.buddy.api.domains.account.dtos.AccountDto;

public interface EmailVerificationService {
    void requestEmail(AccountDto account);

    void confirmEmail(String token);
}
