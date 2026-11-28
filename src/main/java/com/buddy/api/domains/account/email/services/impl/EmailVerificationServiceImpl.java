package com.buddy.api.domains.account.email.services.impl;

import com.buddy.api.commons.configurations.cache.CacheInitializer;
import com.buddy.api.commons.configurations.cache.TokenManager;
import com.buddy.api.commons.exceptions.AccountAlreadyVerifiedException;
import com.buddy.api.commons.exceptions.AccountUnavailableException;
import com.buddy.api.commons.exceptions.NotFoundException;
import com.buddy.api.domains.account.dtos.AccountDto;
import com.buddy.api.domains.account.email.services.AccountValidator;
import com.buddy.api.domains.account.email.services.EmailSender;
import com.buddy.api.domains.account.email.services.EmailVerificationService;
import com.buddy.api.domains.account.services.FindAccount;
import com.buddy.api.domains.account.services.UpdateAccount;
import com.buddy.api.domains.valueobjects.EmailAddress;
import jakarta.annotation.PostConstruct;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
@RequiredArgsConstructor
public class EmailVerificationServiceImpl implements EmailVerificationService {
    private final FindAccount findAccount;
    private final UpdateAccount updateAccount;
    private final CacheInitializer cacheInitializer;
    private final TokenManager tokenManager;
    private final EmailSender emailSender;
    private final AccountValidator accountValidator;

    @PostConstruct
    public void init() {
        cacheInitializer.initializeVerificationTokenCache();
    }

    @Override
    public void requestEmail(final EmailAddress email) {
        try {
            final AccountDto account = findAccount.findByEmail(email.value());
            final String userEmail = account.email().value();
            final UUID accountId = account.accountId();
            log.info("Received request for email verification for account={}", accountId);

            accountValidator.validateAccountNotVerified(account);

            final String token = tokenManager.generateAndStoreToken(userEmail);
            emailSender.dispatchVerificationEmail(accountId, userEmail, token);

            log.info("Verification email request for account={} "
                + "accepted and dispatched for async processing.", accountId);
        } catch (NotFoundException | AccountUnavailableException
                 | AccountAlreadyVerifiedException ex) {
            log.debug("Email verification request processed (account state protected):", ex);
        }
    }

    @Override
    @Transactional
    public void confirmEmail(final String token) {
        log.info("Attempting to confirm email via verification token");
        final String emailFromToken = tokenManager.consumeToken(token);
        final AccountDto account = findAccount.findByEmail(emailFromToken);
        final UUID accountId = account.accountId();

        if (account.isVerified()) {
            log.warn("Account {} is already verified. Ignoring confirmation request.", accountId);
            return;
        }

        updateAccount.updateIsVerified(account.email().value(), true);
        log.info("Email successfully verified for account={}", accountId);
    }
}
