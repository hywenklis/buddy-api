package com.buddy.api.domains.account.services;

import java.time.LocalDateTime;

public interface UpdateAccount {
    void updateLastLogin(String email, LocalDateTime lastLogin);

    void updateIsVerified(String email, Boolean isVerified);
}
