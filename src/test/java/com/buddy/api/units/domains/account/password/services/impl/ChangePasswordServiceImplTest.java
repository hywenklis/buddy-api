package com.buddy.api.units.domains.account.password.services.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.buddy.api.commons.configurations.security.jwt.TokenBlocklistService;
import com.buddy.api.commons.exceptions.InvalidCurrentPasswordException;
import com.buddy.api.commons.exceptions.NotFoundException;
import com.buddy.api.domains.account.email.services.EmailSender;
import com.buddy.api.domains.account.entities.AccountEntity;
import com.buddy.api.domains.account.password.dtos.ChangePasswordDto;
import com.buddy.api.domains.account.password.services.impl.ChangePasswordServiceImpl;
import com.buddy.api.domains.account.repositories.AccountRepository;
import com.buddy.api.domains.audit.enums.SecurityEventType;
import com.buddy.api.domains.audit.services.SecurityAuditService;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
class ChangePasswordServiceImplTest {

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private TokenBlocklistService blocklistService;

    @Mock
    private EmailSender emailSender;

    @Mock
    private SecurityAuditService securityAuditService;

    @InjectMocks
    private ChangePasswordServiceImpl service;

    @Test
    @DisplayName("Should change password, revoke tokens, log audit event, and send email successfully")
    void shouldChangePasswordSuccessfully() {
        UUID accountId = UUID.randomUUID();
        String currentPassword = "oldPassword123";
        String newPassword = "newPassword123";
        String encodedNewPassword = "encodedNewPassword";
        String email = "test@example.com";
        String ipAddress = "192.168.0.1";
        String userAgent = "Mozilla/5.0";

        AccountEntity account = AccountEntity.builder()
            .accountId(accountId)
            .password("encodedOldPassword")
            .build();

        ChangePasswordDto dto = ChangePasswordDto.builder()
            .accountId(accountId)
            .email(email)
            .currentPassword(currentPassword)
            .newPassword(newPassword)
            .ipAddress(ipAddress)
            .userAgent(userAgent)
            .build();

        when(accountRepository.findById(accountId)).thenReturn(Optional.of(account));
        when(passwordEncoder.matches(currentPassword, account.getPassword())).thenReturn(true);
        when(passwordEncoder.encode(newPassword)).thenReturn(encodedNewPassword);

        service.changePassword(dto);

        assertThat(account.getPassword()).isEqualTo(encodedNewPassword);
        verify(accountRepository).save(account);
        verify(blocklistService).revokeAllUserTokens(email);
        verify(securityAuditService).logEvent(accountId, SecurityEventType.PASSWORD_CHANGED, ipAddress, userAgent);
        verify(emailSender).dispatchPasswordChangedNotification(accountId, email);
    }

    @Test
    @DisplayName("Should throw NotFoundException when account is not found")
    void shouldThrowNotFoundExceptionWhenAccountNotFound() {
        UUID accountId = UUID.randomUUID();
        ChangePasswordDto dto = ChangePasswordDto.builder()
            .accountId(accountId)
            .build();

        when(accountRepository.findById(accountId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.changePassword(dto))
            .isInstanceOf(NotFoundException.class)
            .hasMessageContaining("Account not found");
    }

    @Test
    @DisplayName("Should throw InvalidCurrentPasswordException when current password does not match")
    void shouldThrowInvalidCurrentPasswordExceptionWhenPasswordMismatch() {
        UUID accountId = UUID.randomUUID();
        String currentPassword = "wrongPassword";

        AccountEntity account = AccountEntity.builder()
            .accountId(accountId)
            .password("encodedOldPassword")
            .build();

        ChangePasswordDto dto = ChangePasswordDto.builder()
            .accountId(accountId)
            .currentPassword(currentPassword)
            .build();

        when(accountRepository.findById(accountId)).thenReturn(Optional.of(account));
        when(passwordEncoder.matches(currentPassword, account.getPassword())).thenReturn(false);

        assertThatThrownBy(() -> service.changePassword(dto))
            .isInstanceOf(InvalidCurrentPasswordException.class)
            .hasMessageContaining("Invalid current password");
    }
}
