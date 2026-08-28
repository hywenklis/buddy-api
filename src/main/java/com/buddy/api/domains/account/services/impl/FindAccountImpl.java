package com.buddy.api.domains.account.services.impl;

import com.buddy.api.commons.exceptions.AccountBlockedException;
import com.buddy.api.commons.exceptions.AccountNotVerifiedException;
import com.buddy.api.commons.exceptions.AccountUnavailableException;
import com.buddy.api.commons.exceptions.NotFoundException;
import com.buddy.api.domains.account.dtos.AccountDto;
import com.buddy.api.domains.account.mappers.AccountMapper;
import com.buddy.api.domains.account.repositories.AccountRepository;
import com.buddy.api.domains.account.services.FindAccount;
import com.buddy.api.domains.valueobjects.EmailAddress;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class FindAccountImpl implements FindAccount {

    private static final String ACCOUNT_ID_FIELD = "accountId";
    private static final String ACCOUNT_NOT_FOUND_MSG = "Account not found";

    private final AccountRepository accountRepository;
    private final AccountMapper accountMapper;

    @Override
    @Transactional(readOnly = true)
    public Boolean existsById(final UUID accountId) {
        return accountRepository.existsByAccountIdAndIsDeleted(accountId, false);
    }

    @Override
    @Transactional(readOnly = true)
    public AccountDto findById(final UUID accountId) {
        final var account = accountRepository.findById(accountId)
            .orElseThrow(() -> new NotFoundException(ACCOUNT_ID_FIELD, ACCOUNT_NOT_FOUND_MSG));

        return accountMapper.toAccountDto(account);
    }

    @Override
    @Transactional(readOnly = true)
    public AccountDto findActiveById(final UUID accountId) {
        log.info("Searching and validating active account for ID '{}'", accountId);
        final var account = accountRepository.findById(accountId)
            .orElseThrow(() -> new NotFoundException(ACCOUNT_ID_FIELD, ACCOUNT_NOT_FOUND_MSG));

        validateAccountIsNotDeleted(accountId, account.getIsDeleted());
        validateAccountIsNotBlocked(accountId, account.getIsBlocked());
        validateAccountIsVerified(accountId, account.getIsVerified());

        return accountMapper.toAccountDto(account);
    }

    @Override
    @Transactional(readOnly = true)
    public AccountDto findByEmail(final String email) {
        final var account = accountRepository.findByEmail(new EmailAddress(email))
            .orElseThrow(() -> new NotFoundException("email", ACCOUNT_NOT_FOUND_MSG));

        Optional.of(account)
            .filter(a -> !Boolean.TRUE.equals(a.getIsBlocked())
                && !Boolean.TRUE.equals(a.getIsDeleted()))
            .orElseThrow(() -> {
                log.info("Account unavailable: email={}, blocked={}, deleted={}",
                    email, account.getIsBlocked(), account.getIsDeleted());
                return new AccountUnavailableException("credentials", "Account is not available");
            });

        return accountMapper.toAccountDto(account);
    }

    @Override
    @Transactional(readOnly = true)
    public AccountDto findAccountForAuthentication(final String email) {
        final var account = accountRepository.findByEmail(new EmailAddress(email))
            .orElseThrow(() -> new NotFoundException("email", ACCOUNT_NOT_FOUND_MSG));

        return accountMapper.toAccountDto(account);
    }

    private void validateAccountIsNotDeleted(final UUID accountId, final Boolean isDeleted) {
        Optional.of(Boolean.TRUE.equals(isDeleted))
            .filter(deleted -> !deleted)
            .orElseThrow(() -> {
                log.warn("Account '{}' is deleted", accountId);
                return new AccountUnavailableException(
                    ACCOUNT_ID_FIELD, "Account is not available");
            });
    }

    private void validateAccountIsNotBlocked(final UUID accountId, final Boolean isBlocked) {
        Optional.of(Boolean.TRUE.equals(isBlocked))
            .filter(blocked -> !blocked)
            .orElseThrow(() -> {
                log.warn("Account '{}' is blocked", accountId);
                return new AccountBlockedException(ACCOUNT_ID_FIELD, "Account is blocked");
            });
    }

    private void validateAccountIsVerified(final UUID accountId, final Boolean isVerified) {
        Optional.of(Boolean.FALSE.equals(isVerified))
            .filter(notVerified -> !notVerified)
            .orElseThrow(() -> {
                log.warn("Account '{}' is not verified", accountId);
                return new AccountNotVerifiedException(ACCOUNT_ID_FIELD, "Account is not verified");
            });
    }
}
