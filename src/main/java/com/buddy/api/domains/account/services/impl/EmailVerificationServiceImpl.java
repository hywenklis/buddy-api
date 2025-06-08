package com.buddy.api.domains.account.services.impl;

import com.buddy.api.commons.exceptions.AuthenticationException;
import com.buddy.api.commons.exceptions.TooManyRequestsException;
import com.buddy.api.domains.account.dtos.AccountDto;
import com.buddy.api.domains.account.entities.AccountEntity;
import com.buddy.api.domains.account.repositories.AccountRepository;
import com.buddy.api.integrations.clients.email.EmailService;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailVerificationServiceImpl {

    private final AccountRepository accountRepository;
    private final EmailService emailService;
    private final EmailVerificationCacheService cache;

    public void requestVerificationEmail(final AccountDto account) {
        UUID accountId = account.accountId();

        if (Boolean.TRUE.equals(cache.isRateLimited(accountId))) {
            throw new TooManyRequestsException("Please wait before requesting another code");
        }

        String token = UUID.randomUUID().toString();
        cache.storeToken(token, accountId);
        cache.setRateLimit(accountId);

        log.info("Sending verification email to {} (token={})", account.email().value(), token);
        emailService.sendVerificationEmail(account.email().value(), token);
    }

    public void confirmEmail(final String token) {
        UUID accountId = cache.getAccountIdByToken(token);

        if (accountId == null) {
            throw new AuthenticationException("Invalid or expired token", "token");
        }

        AccountEntity account = accountRepository.findById(accountId)
            .orElseThrow(() -> new RuntimeException("Account not found"));

        account.setIsVerified(true);
        accountRepository.save(account);

        cache.invalidateToken(token);
        log.info("Account {} marked as verified", accountId);
    }
}
