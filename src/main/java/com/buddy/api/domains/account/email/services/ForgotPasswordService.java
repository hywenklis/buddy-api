package com.buddy.api.domains.account.email.services;

public interface ForgotPasswordService {
    void requestPasswordRecovery(String email);
}
