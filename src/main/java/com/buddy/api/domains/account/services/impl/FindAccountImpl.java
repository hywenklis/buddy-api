package com.buddy.api.domains.account.services.impl;

import com.buddy.api.domains.account.repository.AccountRepository;
import com.buddy.api.domains.account.services.FindAccount;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class FindAccountImpl implements FindAccount {

    private final AccountRepository accountRepository;

    @Override
    @Transactional(readOnly = true)
    public Boolean existsById(final UUID accountId) {
        return accountRepository.existsByAccountIdAndIsDeleted(accountId, false);
    }
}
