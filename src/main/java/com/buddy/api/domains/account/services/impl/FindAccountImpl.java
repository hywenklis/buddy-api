package com.buddy.api.domains.account.services.impl;

import com.buddy.api.commons.exceptions.BlockedOrDeletedAccountException;
import com.buddy.api.commons.exceptions.NotFoundException;
import com.buddy.api.domains.account.dtos.AccountDto;
import com.buddy.api.domains.account.mappers.AccountMapper;
import com.buddy.api.domains.account.repositories.AccountRepository;
import com.buddy.api.domains.account.services.FindAccount;
import com.buddy.api.domains.valueobjects.EmailAddress;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class FindAccountImpl implements FindAccount {

    private final AccountRepository accountRepository;
    private final AccountMapper accountMapper;

    @Override
    @Transactional(readOnly = true)
    public Boolean existsById(final UUID accountId) {
        return accountRepository.existsByAccountIdAndIsDeleted(accountId, false);
    }

    @Override
    @Transactional(readOnly = true)
    public AccountDto findByEmail(final String email) {
        var account = accountRepository.findByEmail(new EmailAddress(email))
            .orElseThrow(() -> new NotFoundException("email", "Account not found"));

        if (account.getIsBlocked() || account.getIsDeleted()) {
            throw new BlockedOrDeletedAccountException("email", "Account is blocked or deleted");
        }

        return accountMapper.toAccountDto(account);
    }
}
