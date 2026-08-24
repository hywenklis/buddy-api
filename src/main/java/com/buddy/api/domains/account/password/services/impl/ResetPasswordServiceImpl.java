package com.buddy.api.domains.account.password.services.impl;

import com.buddy.api.commons.configurations.cache.ForgotPasswordTokenManager;
import com.buddy.api.commons.configurations.security.jwt.TokenBlocklistService;
import com.buddy.api.commons.exceptions.NotFoundException;
import com.buddy.api.domains.account.password.dtos.ResetPasswordDto;
import com.buddy.api.domains.account.password.services.ResetPasswordService;
import com.buddy.api.domains.account.services.UpdateAccount;
import com.buddy.api.domains.valueobjects.EmailAddress;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@Service
@RequiredArgsConstructor
public class ResetPasswordServiceImpl implements ResetPasswordService {

    private final ForgotPasswordTokenManager tokenManager;
    private final UpdateAccount updateAccount;
    private final TokenBlocklistService blocklistService;
    private final PasswordEncoder passwordEncoder;
    private final ApplicationEventPublisher eventPublisher;

    @Override
    @Transactional
    public void resetPassword(final ResetPasswordDto request) {
        log.info("Starting reset password process for token");
        final var email = tokenManager.consumeToken(request.token());
        
        if (email == null) {
            log.warn("Invalid or expired reset token provided");
            throw new NotFoundException("token", "Invalid or expired reset token");
        }

        log.info("Resetting password for user");
        
        final var encodedPassword = passwordEncoder.encode(request.newPassword());
        updateAccount.updatePassword(new EmailAddress(email), encodedPassword);
        
        eventPublisher.publishEvent(new PasswordResetEvent(email));
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handlePasswordReset(final PasswordResetEvent event) {
        blocklistService.revokeAllUserTokens(event.email());
        log.info("Password successfully reset and all previous tokens revoked");
    }

    public record PasswordResetEvent(String email) {
    }
}
