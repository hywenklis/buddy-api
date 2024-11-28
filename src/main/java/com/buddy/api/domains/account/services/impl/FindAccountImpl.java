package com.buddy.api.domains.account.services.impl;

import com.buddy.api.domains.account.repository.AccountRepository;
import com.buddy.api.domains.account.services.FindAccount;
import java.util.UUID;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class FindAccountImpl implements FindAccount {

    private final AccountRepository accountRepository;

    @Override
    public Boolean existsById(final UUID accountId) {
        return accountRepository
            .findById(accountId)
            .filter(accountEntity -> !accountEntity.getIsDeleted())
            .isPresent();
    }
}
