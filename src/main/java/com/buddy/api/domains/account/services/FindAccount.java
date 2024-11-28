package com.buddy.api.domains.account.services;

import java.util.UUID;

public interface FindAccount {
    Boolean existsById(UUID accountId);
}
