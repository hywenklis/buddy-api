package com.buddy.api.domains.account.email.services;

import com.buddy.api.domains.valueobjects.EmailAddress;

public interface ForgotPasswordService {
    void requestPasswordRecovery(EmailAddress email);
}
