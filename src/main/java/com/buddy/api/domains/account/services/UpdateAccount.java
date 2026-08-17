package com.buddy.api.domains.account.services;

import com.buddy.api.domains.valueobjects.EmailAddress;
import java.time.LocalDateTime;

public interface UpdateAccount {
    void updateLastLogin(String email, LocalDateTime lastLogin);

    void updateIsVerified(String email, Boolean isVerified);

    void updatePassword(EmailAddress email, String encodedPassword);
}
