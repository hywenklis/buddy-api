package com.buddy.api.domains.account.email.services.impls;

import static java.util.Objects.isNull;

import com.buddy.api.commons.configurations.properties.EmailProperties;
import com.buddy.api.commons.configurations.properties.RateLimitProperties;
import com.buddy.api.commons.exceptions.AccountAlreadyVerifiedException;
import com.buddy.api.commons.exceptions.AuthenticationException;
import com.buddy.api.commons.exceptions.CacheInitializationException;
import com.buddy.api.commons.exceptions.NotFoundException;
import com.buddy.api.commons.exceptions.TooManyRequestsException;
import com.buddy.api.domains.account.dtos.AccountDto;
import com.buddy.api.domains.account.email.services.EmailTemplateLoaderService;
import com.buddy.api.domains.account.email.services.EmailVerificationService;
import com.buddy.api.domains.account.services.UpdateAccount;
import com.buddy.api.integrations.clients.manager.ManagerService;
import jakarta.annotation.PostConstruct;
import java.time.Duration;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
@RequiredArgsConstructor
public class EmailVerificationServiceImpl implements EmailVerificationService {
    private static final String VERIFICATION_TOKEN_CACHE_NAME = "emailVerificationToken";
    private static final String RATE_LIMIT_CACHE_NAME = "emailVerificationRateLimit";
    private static final String RATE_LIMIT_COUNT_KEY_PREFIX = "rate-limit:count:";

    private final UpdateAccount updateAccount;
    private final ManagerService managerService;
    private final CacheManager cacheManager;
    private final EmailProperties emailProperties;
    private final RateLimitProperties rateLimitProperties;
    private final EmailTemplateLoaderService emailTemplateLoader;
    private final RedisTemplate<String, String> redisTemplate;

    private Cache verificationTokenCache;

    @PostConstruct
    public void init() {
        this.verificationTokenCache = cacheManager.getCache(VERIFICATION_TOKEN_CACHE_NAME);
        Cache rateLimitCache = cacheManager.getCache(RATE_LIMIT_CACHE_NAME);

        if (verificationTokenCache == null || rateLimitCache == null) {
            throw new CacheInitializationException(
                "cache",
                "Required caches not found: "
                    + "emailVerificationToken or emailVerificationRateLimit");
        }
    }

    @Override
    public void requestEmail(final AccountDto account) {
        final String userEmail = account.email().value();
        final UUID accountId = account.accountId();
        log.info("Received request for email verification for account={}", accountId);

        validateAccountNotVerified(account);
        checkRateLimit(userEmail, accountId);

        final String token = UUID.randomUUID().toString();
        verificationTokenCache.put(token, userEmail);

        dispatchVerificationEmail(accountId, userEmail, token);

        log.info("Verification email request for account={} "
            + "accepted and dispatched for async processing.", accountId);
    }

    @Async
    public void dispatchVerificationEmail(final UUID accountId,
                                          final String userEmail,
                                          final String token
    ) {
        try {
            final String verificationUrl = emailProperties.templates().url() + token;
            final String htmlBody = buildConfirmationEmailBody(verificationUrl);

            log.info("Sending verification email to account={}", accountId);
            managerService.sendEmailNotification(
                List.of(userEmail),
                emailProperties.templates().from(),
                emailProperties.templates().subject(),
                htmlBody
            );
            log.info("Verification email successfully sent to account={}", accountId);
        } catch (Exception e) {
            log.error("Failed to send email verification for account={}", accountId, e);
            verificationTokenCache.evict(token);
        }
    }

    @Override
    @Transactional
    public void confirmEmail(final String token, final AccountDto account) {
        final UUID accountId = account.accountId();
        log.info("Attempting to confirm email for account={}", accountId);

        final String emailFromToken = validateAndGetTokenEmail(token, accountId);
        validateTokenMatchesAccount(account, emailFromToken, accountId);

        if (account.isVerified()) {
            log.warn("Account {} is already verified. Ignoring confirmation request.", accountId);
            verificationTokenCache.evict(token);
            return;
        }

        updateAccount.updateIsVerified(account.email().value(), true);
        verificationTokenCache.evict(token);

        log.info("Email successfully verified for account={}", accountId);
    }

    private void validateAccountNotVerified(final AccountDto account) {
        if (account.isVerified()) {
            throw new AccountAlreadyVerifiedException(
                "account.status",
                "This account is already verified."
            );
        }
    }

    private void checkRateLimit(final String email, final UUID accountId) {
        String countKey = RATE_LIMIT_COUNT_KEY_PREFIX + email;
        Long count = redisTemplate.opsForValue().increment(countKey, 1);

        if (!isNull(count) && count == 1) {
            redisTemplate.expire(countKey, Duration.ofMinutes(rateLimitProperties.windowMinutes()));
        }

        if (!isNull(count) && count > rateLimitProperties.maxAttempts()) {
            log.warn("Rate limit exceeded for email verification request for account={}",
                accountId);
            throw new TooManyRequestsException(
                "Too many verification requests. Please wait a minute before trying again."
            );
        }
    }

    private String validateAndGetTokenEmail(final String token, final UUID accountId) {
        String email = verificationTokenCache.get(token, String.class);
        if (email == null) {
            log.warn("Invalid or expired token for account={}. Token: {}",
                accountId, token.substring(0, Math.min(token.length(), 8)) + "...");
            throw new NotFoundException("token", "Invalid or expired verification token.");
        }
        return email;
    }

    private void validateTokenMatchesAccount(final AccountDto account, final String emailFromToken,
                                             final UUID accountId) {
        if (!account.email().value().equals(emailFromToken)) {
            log.error("Token email does not match authenticated user account={}", accountId);
            throw new AuthenticationException("Token does not belong to the authenticated user",
                "token");
        }
    }

    private String buildConfirmationEmailBody(final String verificationUrl) {
        String template = emailTemplateLoader.load(emailProperties.templates().templatePath());
        return template.replace("{{url}}", verificationUrl);
    }
}
