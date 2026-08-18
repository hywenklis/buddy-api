package com.buddy.api.domains.account.password.services.impl;

import com.buddy.api.commons.configurations.security.jwt.TokenBlocklistService;
import com.buddy.api.commons.exceptions.InvalidCurrentPasswordException;
import com.buddy.api.commons.exceptions.NotFoundException;
import com.buddy.api.domains.account.email.services.EmailSender;
import com.buddy.api.domains.account.password.dtos.ChangePasswordDto;
import com.buddy.api.domains.account.password.services.ChangePasswordService;
import com.buddy.api.domains.account.repositories.AccountRepository;
import com.buddy.api.domains.audit.enums.SecurityEventType;
import com.buddy.api.domains.audit.services.SecurityAuditService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@RequiredArgsConstructor
@Service
public class ChangePasswordServiceImpl implements ChangePasswordService {

    private final AccountRepository accountRepository;
    private final PasswordEncoder passwordEncoder;
    private final TokenBlocklistService blocklistService;
    private final EmailSender emailSender;
    private final SecurityAuditService securityAuditService;

    @Override
    @Transactional
    public void changePassword(final ChangePasswordDto dto) {
        log.info("Changing password for account: {}", dto.accountId());

        final var account = accountRepository.findById(dto.accountId())
            .orElseThrow(() -> new NotFoundException("accountId", "Account not found"));

        if (!passwordEncoder.matches(dto.currentPassword(), account.getPassword())) {
            log.warn("Invalid current password for account: {}", dto.accountId());
            throw new InvalidCurrentPasswordException();
        }

        account.setPassword(passwordEncoder.encode(dto.newPassword()));
        accountRepository.save(account);

        blocklistService.revokeAllUserTokens(dto.email());
        log.info("All tokens revoked for user: {}", dto.email());

        securityAuditService.logEvent(
            dto.accountId(),
            SecurityEventType.PASSWORD_CHANGED,
            dto.ipAddress(),
            dto.userAgent()
        );

        emailSender.dispatchPasswordChangedNotification(dto.accountId(), dto.email());
    }
}
