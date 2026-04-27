package com.buddy.api.domains.account.email.services;

import com.buddy.api.domains.account.dtos.AccountDto;

public interface ForgotPasswordService {
    void requestPasswordRecovery(AccountDto account);
}
