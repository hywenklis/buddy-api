package com.buddy.api.domains.account.services;

import java.time.LocalDateTime;
import java.util.UUID;

public interface UpdateAccount {
    void updateLastLogin(UUID accountId, LocalDateTime lastLogin);
}
