package com.buddy.api.domains.account.email.services.impls;

import com.buddy.api.commons.configurations.cache.CacheInitializer;
import com.buddy.api.commons.configurations.cache.RateLimitChecker;
import com.buddy.api.commons.configurations.cache.TokenManager;
import com.buddy.api.domains.account.dtos.AccountDto;
import com.buddy.api.domains.account.email.services.AccountValidator;
import com.buddy.api.domains.account.email.services.EmailSender;
import com.buddy.api.domains.account.email.services.EmailVerificationService;
import com.buddy.api.domains.account.services.UpdateAccount;
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
    private final UpdateAccount updateAccount;
    private final CacheInitializer cacheInitializer;
    private final RateLimitChecker rateLimitChecker;
    private final TokenManager tokenManager;
    private final EmailSender emailSender;
    private final AccountValidator accountValidator;

    @PostConstruct
    public void init() {
        cacheInitializer.initializeVerificationTokenCache();
    }

    @Override
    public void requestEmail(final AccountDto account) {
        String userEmail = account.email().value();
        UUID accountId = account.accountId();
        log.info("Received request for email verification for account={}", accountId);

        accountValidator.validateAccountNotVerified(account);
        rateLimitChecker.checkRateLimit(userEmail, accountId);

        String token = tokenManager.generateAndStoreToken(userEmail);
        emailSender.dispatchVerificationEmail(accountId, userEmail, token);

        log.info("Verification email request for account={} "
            + "accepted and dispatched for async processing.", accountId);
    }

    @Override
    @Transactional
    public void confirmEmail(final String token, final AccountDto account) {
        UUID accountId = account.accountId();
        log.info("Attempting to confirm email for account={}", accountId);

        String emailFromToken = tokenManager.validateAndGetTokenEmail(token, accountId);
        accountValidator.validateTokenMatchesAccount(account, emailFromToken, accountId);

        if (account.isVerified()) {
            log.warn("Account {} is already verified. Ignoring confirmation request.", accountId);
            tokenManager.evictToken(token);
            return;
        }

        updateAccount.updateIsVerified(account.email().value(), true);
        tokenManager.evictToken(token);

        log.info("Email successfully verified for account={}", accountId);
    }
}
