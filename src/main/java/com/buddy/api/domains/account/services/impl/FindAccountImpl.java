package com.buddy.api.domains.account.services.impl;

import com.buddy.api.commons.exceptions.AccountUnavailableException;
import com.buddy.api.commons.exceptions.NotFoundException;
import com.buddy.api.domains.account.dtos.AccountDto;
import com.buddy.api.domains.account.mappers.AccountMapper;
import com.buddy.api.domains.account.repositories.AccountRepository;
import com.buddy.api.domains.account.services.FindAccount;
import com.buddy.api.domains.valueobjects.EmailAddress;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
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
        final var account = accountRepository.findByEmail(new EmailAddress(email))
            .orElseThrow(() -> new NotFoundException("email", "Account not found"));

        if (account.getIsBlocked() || account.getIsDeleted()) {
            log.info("Account unavailable: email={}, blocked={}, deleted={}",
                email, account.getIsBlocked(), account.getIsDeleted()
            );

            throw new AccountUnavailableException("credentials",
                "Account is not available");
        }

        return accountMapper.toAccountDto(account);
    }

    @Override
    @Transactional(readOnly = true)
    public AccountDto findAccountForAuthentication(final String email) {
        final var account = accountRepository.findByEmail(new EmailAddress(email))
            .orElseThrow(() -> new NotFoundException("email", "Account not found"));

        return accountMapper.toAccountDto(account);
    }
}
