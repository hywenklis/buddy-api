package com.buddy.api.domains.account.email.services.impl;

import com.buddy.api.commons.configurations.cache.ForgotPasswordTokenManager;
import com.buddy.api.commons.configurations.cache.RateLimitChecker;
import com.buddy.api.domains.account.dtos.AccountDto;
import com.buddy.api.domains.account.email.services.EmailSender;
import com.buddy.api.domains.account.email.services.ForgotPasswordService;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class ForgotPasswordServiceImpl implements ForgotPasswordService {
    private final RateLimitChecker rateLimitChecker;
    private final ForgotPasswordTokenManager forgotPasswordTokenManager;
    private final EmailSender emailSender;

    @Override
    public void requestPasswordRecovery(final AccountDto account) {
        if (account == null) {
            log.debug(
                "Password recovery requested for non-existent email. "
                + "Silently ignoring (enumeration protection).");
            return;
        }

        String userEmail = account.email().value();
        UUID accountId = account.accountId();
        log.info("Received request for password recovery for account={}", accountId);

        rateLimitChecker.checkPasswordRecoveryRateLimit(userEmail, accountId);

        String token = forgotPasswordTokenManager.generateAndStoreToken(userEmail);

        emailSender.dispatchPasswordRecoveryEmail(accountId, userEmail, token);

        log.info("Password recovery email request for account={} "
            + "dispatched for async processing.", accountId);
    }
}
