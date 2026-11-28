package com.buddy.api.domains.account.email.services;

import com.buddy.api.domains.valueobjects.EmailAddress;

public interface EmailVerificationService {
    void requestEmail(EmailAddress email);

    void confirmEmail(String token);
}
