package com.buddy.api.domains.account.email.services.impls;

import com.buddy.api.commons.configurations.properties.EmailProperties;
import com.buddy.api.commons.exceptions.AccountAlreadyVerifiedException;
import com.buddy.api.commons.exceptions.NotFoundException;
import com.buddy.api.commons.exceptions.TooManyRequestsException;
import com.buddy.api.domains.account.dtos.AccountDto;
import com.buddy.api.domains.account.email.services.EmailVerificationService;
import com.buddy.api.domains.account.services.UpdateAccount;
import com.buddy.api.integrations.clients.notification.EmailNotificationService;
import jakarta.annotation.PostConstruct;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
@RequiredArgsConstructor
public class EmailVerificationServiceImpl implements EmailVerificationService {
    private static final String VERIFICATION_TOKEN_CACHE_NAME = "emailVerificationToken";
    private static final String RATE_LIMIT_CACHE_NAME = "emailVerificationRateLimit";

    private final UpdateAccount updateAccount;
    private final EmailNotificationService emailNotificationService;
    private final CacheManager cacheManager;
    private final EmailProperties emailProperties;

    private Cache verificationTokenCache;
    private Cache rateLimitCache;

    @PostConstruct
    public void init() {
        this.verificationTokenCache = Objects.requireNonNull(
            cacheManager.getCache(VERIFICATION_TOKEN_CACHE_NAME),
            "Cache 'emailVerificationToken' not found."
        );
        this.rateLimitCache = Objects.requireNonNull(
            cacheManager.getCache(RATE_LIMIT_CACHE_NAME),
            "Cache 'emailVerificationRateLimit' not found."
        );
    }

    @Override
    public void requestEmail(final AccountDto account) {
        final String userEmail = account.email().value();
        log.info("Received request for email verification for: {}", userEmail);

        if (Boolean.TRUE.equals(account.isVerified())) {
            throw new AccountAlreadyVerifiedException(
                "account.status",
                "This account is already verified."
            );
        }

        checkRateLimit(userEmail);

        final String token = UUID.randomUUID().toString();
        verificationTokenCache.put(token, userEmail);
        rateLimitCache.put(userEmail, "rate-limited");

        final String verificationUrl = emailProperties.templates().url() + token;
        final String subject = emailProperties.templates().subject();
        final String htmlBody = buildConfirmationEmailBody(verificationUrl);

        emailNotificationService.sendEmail(List.of(userEmail), subject, htmlBody);

        log.info("Verification email request processed for: {}", userEmail);
    }

    @Override
    @Transactional
    public void confirmEmail(final String token, final AccountDto account) {
        log.info("Attempting to confirm email with token.");

        Cache.ValueWrapper valueWrapper = verificationTokenCache.get(token);
        if (valueWrapper == null || valueWrapper.get() == null) {
            throw new NotFoundException("token", "Invalid or expired verification token.");
        }

        final String email = (String) valueWrapper.get();

        if (Boolean.TRUE.equals(account.isVerified())) {
            log.warn("Account {} is already verified. Ignoring confirmation request.", email);
            verificationTokenCache.evict(token);
            return;
        }

        updateAccount.updateIsVerified(account.email().value(), true);
        verificationTokenCache.evict(token);

        log.info("Email successfully verified for account: {}", email);
    }

    private void checkRateLimit(final String email) {
        if (rateLimitCache.get(email) != null) {
            log.warn("Rate limit exceeded for email verification request: {}", email);
            throw new TooManyRequestsException(
                "You have requested a verification email recently. "
                    + "Please wait a minute before trying again."
            );
        }
    }

    private String buildConfirmationEmailBody(final String verificationUrl) {
        return emailProperties.templates().verification().replace("%s", verificationUrl);
    }
}
