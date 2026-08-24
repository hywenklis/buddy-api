package com.buddy.api.domains.account.password.services.impl;

import com.buddy.api.commons.configurations.security.jwt.TokenBlocklistService;
import com.buddy.api.commons.exceptions.DomainException;
import com.buddy.api.commons.exceptions.InvalidCurrentPasswordException;
import com.buddy.api.domains.account.email.services.EmailSender;
import com.buddy.api.domains.account.password.dtos.ChangePasswordDto;
import com.buddy.api.domains.account.password.services.ChangePasswordService;
import com.buddy.api.domains.account.services.FindAccount;
import com.buddy.api.domains.account.services.UpdateAccount;
import com.buddy.api.domains.audit.enums.SecurityEventType;
import com.buddy.api.domains.audit.services.SecurityAuditService;
import com.buddy.api.domains.valueobjects.EmailAddress;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@RequiredArgsConstructor
@Service
public class ChangePasswordServiceImpl implements ChangePasswordService {

    private final FindAccount findAccount;
    private final UpdateAccount updateAccount;
    private final PasswordEncoder passwordEncoder;
    private final TokenBlocklistService blocklistService;
    private final EmailSender emailSender;
    private final SecurityAuditService securityAuditService;
    private final ApplicationEventPublisher eventPublisher;

    @Override
    @Transactional
    public void changePassword(final ChangePasswordDto dto) {
        log.info("Changing password for account: {}", dto.accountId());


        final var accountDto = findAccount.findById(dto.accountId());

        if (!passwordEncoder.matches(dto.currentPassword(), accountDto.password())) {
            log.warn("Invalid current password for account: {}", dto.accountId());
            throw new InvalidCurrentPasswordException();
        }

        if (passwordEncoder.matches(dto.newPassword(), accountDto.password())) {
            throw new DomainException(
                "New password cannot be the same as current password",
                "newPassword",
                HttpStatus.BAD_REQUEST,
                null
            );
        }

        updateAccount.updatePassword(new EmailAddress(dto.email()),
            passwordEncoder.encode(dto.newPassword()));

        eventPublisher.publishEvent(new PasswordChangedEvent(
            dto.accountId(),
            new EmailAddress(dto.email()),
            dto.ipAddress(),
            dto.userAgent()
        ));
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handlePasswordChanged(final PasswordChangedEvent event) {
        blocklistService.revokeAllUserTokens(event.email().value());
        log.info("All tokens revoked for account: {}", event.accountId());

        securityAuditService.logEvent(
            event.accountId(),
            SecurityEventType.PASSWORD_CHANGED,
            event.ipAddress(),
            event.userAgent()
        );

        emailSender.dispatchPasswordChangedNotification(event.accountId(), event.email());
    }

    public record PasswordChangedEvent(
        java.util.UUID accountId,
        EmailAddress email,
        String ipAddress,
        String userAgent
    ) {
    }
}
