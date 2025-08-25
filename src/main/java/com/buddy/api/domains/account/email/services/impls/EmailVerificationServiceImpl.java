package com.buddy.api.domains.account.email.services.impls;

import com.buddy.api.commons.configurations.properties.EmailProperties;
import com.buddy.api.commons.exceptions.AccountAlreadyVerifiedException;
import com.buddy.api.commons.exceptions.AuthenticationException;
import com.buddy.api.commons.exceptions.NotFoundException;
import com.buddy.api.commons.exceptions.TooManyRequestsException;
import com.buddy.api.domains.account.dtos.AccountDto;
import com.buddy.api.domains.account.email.services.EmailTemplateLoaderService;
import com.buddy.api.domains.account.email.services.EmailVerificationService;
import com.buddy.api.domains.account.services.UpdateAccount;
import com.buddy.api.domains.profile.dtos.ProfileDto;
import com.buddy.api.domains.profile.services.FindProfile;
import com.buddy.api.integrations.clients.manager.ManagerService;
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
    private final ManagerService managerService;
    private final CacheManager cacheManager;
    private final EmailProperties emailProperties;
    private final EmailTemplateLoaderService emailTemplateLoader;
    private final FindProfile findProfile;

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

        final List<ProfileDto> profiles = findProfile.findByAccountEmail(userEmail);
        final String name = profiles.isEmpty() ? "Buddy" : profiles.getFirst().name();

        final String verificationUrl = emailProperties.templates().url() + token;
        final String htmlBody = buildConfirmationEmailBody(verificationUrl, name);

        managerService.sendEmailNotification(
            List.of(userEmail),
            emailProperties.templates().from(),
            emailProperties.templates().subject(),
            htmlBody
        );

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

        final String emailFromToken = (String) valueWrapper.get();

        if (!account.email().value().equals(emailFromToken)) {
            log.error("Token email [{}] does not match authenticated user email [{}]",
                emailFromToken, account.email().value());
            throw new AuthenticationException("Token does not belong to the authenticated user",
                "token");
        }

        if (Boolean.TRUE.equals(account.isVerified())) {
            log.warn("Account {} is already verified. Ignoring confirmation request.",
                emailFromToken);

            verificationTokenCache.evict(token);
            return;
        }

        updateAccount.updateIsVerified(account.email().value(), true);
        verificationTokenCache.evict(token);

        log.info("Email successfully verified for account: {}", emailFromToken);
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

    private String buildConfirmationEmailBody(final String verificationUrl, final String name) {
        String template = emailTemplateLoader.load(emailProperties.templates().templatePath());
        return template.replace("{{url}}", verificationUrl).replace("{{name}}", name);
    }
}
