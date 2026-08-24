package com.buddy.api.units.domains.account.password.services.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.buddy.api.commons.configurations.security.jwt.TokenBlocklistService;
import com.buddy.api.commons.exceptions.DomainException;
import com.buddy.api.commons.exceptions.InvalidCurrentPasswordException;
import com.buddy.api.commons.exceptions.NotFoundException;
import com.buddy.api.domains.account.dtos.AccountDto;
import com.buddy.api.domains.account.email.services.EmailSender;
import com.buddy.api.domains.account.password.dtos.ChangePasswordDto;
import com.buddy.api.domains.account.password.services.impl.ChangePasswordServiceImpl;
import com.buddy.api.domains.account.services.FindAccount;
import com.buddy.api.domains.account.services.UpdateAccount;
import com.buddy.api.domains.audit.enums.SecurityEventType;
import com.buddy.api.domains.audit.services.SecurityAuditService;
import com.buddy.api.domains.valueobjects.EmailAddress;
import com.buddy.api.utils.RandomEmailUtils;
import java.util.UUID;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
class ChangePasswordServiceImplTest {

    @Mock
    private FindAccount findAccount;

    @Mock
    private UpdateAccount updateAccount;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private TokenBlocklistService blocklistService;

    @Mock
    private EmailSender emailSender;

    @Mock
    private SecurityAuditService securityAuditService;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @InjectMocks
    private ChangePasswordServiceImpl service;

    @Test
    @DisplayName("Should change password and publish a password changed event")
    void shouldChangePasswordSuccessfully() {
        UUID accountId = UUID.randomUUID();
        String currentPassword = RandomStringUtils.secure().nextAlphanumeric(10);
        String newPassword = RandomStringUtils.secure().nextAlphanumeric(10);
        String encodedNewPassword = RandomStringUtils.secure().nextAlphanumeric(15);
        String email = RandomEmailUtils.generateValidEmail();
        String ipAddress = "192.168.0." + RandomStringUtils.secure().nextNumeric(1, 3);
        String userAgent = RandomStringUtils.secure().nextAlphanumeric(20);

        AccountDto accountDto = AccountDto.builder()
            .accountId(accountId)
            .password(RandomStringUtils.secure().nextAlphanumeric(15))
            .build();

        final ChangePasswordDto dto = ChangePasswordDto.builder()
            .accountId(accountId)
            .email(email)
            .currentPassword(currentPassword)
            .newPassword(newPassword)
            .ipAddress(ipAddress)
            .userAgent(userAgent)
            .build();

        when(findAccount.findById(accountId)).thenReturn(accountDto);
        when(passwordEncoder.matches(currentPassword, accountDto.password())).thenReturn(true);
        when(passwordEncoder.encode(newPassword)).thenReturn(encodedNewPassword);

        service.changePassword(dto);

        verify(updateAccount).updatePassword(new EmailAddress(email), encodedNewPassword);
        ArgumentCaptor<ChangePasswordServiceImpl.PasswordChangedEvent> eventCaptor =
            ArgumentCaptor.forClass(ChangePasswordServiceImpl.PasswordChangedEvent.class);
        verify(eventPublisher).publishEvent(eventCaptor.capture());

        assertThat(eventCaptor.getValue().accountId()).isEqualTo(accountId);
        assertThat(eventCaptor.getValue().email()).isEqualTo(new EmailAddress(email));
        assertThat(eventCaptor.getValue().ipAddress()).isEqualTo(ipAddress);
        assertThat(eventCaptor.getValue().userAgent()).isEqualTo(userAgent);
        verifyNoInteractions(blocklistService, securityAuditService, emailSender);
    }

    @Test
    @DisplayName("Should apply password change effects after transaction commit event")
    void shouldApplyPasswordChangeEffectsAfterTransactionCommitEvent() {
        UUID accountId = UUID.randomUUID();
        String email = RandomEmailUtils.generateValidEmail();
        String ipAddress = "192.168.0." + RandomStringUtils.secure().nextNumeric(1, 3);
        String userAgent = RandomStringUtils.secure().nextAlphanumeric(20);
        ChangePasswordServiceImpl.PasswordChangedEvent event =
            new ChangePasswordServiceImpl.PasswordChangedEvent(
                accountId,
                new EmailAddress(email),
                ipAddress,
                userAgent
            );

        service.handlePasswordChanged(event);

        verify(blocklistService).revokeAllUserTokens(new EmailAddress(email).value());
        verify(securityAuditService).logEvent(accountId, SecurityEventType.PASSWORD_CHANGED,
            ipAddress, userAgent);
        verify(emailSender).dispatchPasswordChangedNotification(accountId,
            new EmailAddress(email));
    }

    @Test
    @DisplayName("Should throw NotFoundException when account is not found")
    void shouldThrowNotFoundExceptionWhenAccountNotFound() {
        UUID accountId = UUID.randomUUID();
        ChangePasswordDto dto = ChangePasswordDto.builder()
            .accountId(accountId)
            .email(RandomEmailUtils.generateValidEmail())
            .build();

        when(findAccount.findById(accountId)).thenThrow(
            new NotFoundException("accountId", "Account not found"));

        assertThatThrownBy(() -> service.changePassword(dto))
            .isInstanceOf(NotFoundException.class)
            .hasMessageContaining("Account not found");
        verifyNoInteractions(updateAccount, passwordEncoder, blocklistService,
            securityAuditService, emailSender, eventPublisher);
    }

    @Test
    @DisplayName("Should throw InvalidCurrentPasswordException when "
        + "current password does not match")
    void shouldThrowInvalidCurrentPasswordExceptionWhenPasswordMismatch() {
        UUID accountId = UUID.randomUUID();
        String currentPassword = RandomStringUtils.secure().nextAlphanumeric(10);

        AccountDto accountDto = AccountDto.builder()
            .accountId(accountId)
            .password(RandomStringUtils.secure().nextAlphanumeric(15))
            .build();

        ChangePasswordDto dto = ChangePasswordDto.builder()
            .accountId(accountId)
            .email(RandomEmailUtils.generateValidEmail())
            .currentPassword(currentPassword)
            .build();

        when(findAccount.findById(accountId)).thenReturn(accountDto);
        when(passwordEncoder.matches(currentPassword, accountDto.password())).thenReturn(false);

        assertThatThrownBy(() -> service.changePassword(dto))
            .isInstanceOf(InvalidCurrentPasswordException.class)
            .hasMessageContaining("Invalid current password");
        verifyNoInteractions(updateAccount, blocklistService, securityAuditService, emailSender,
            eventPublisher);
        verify(passwordEncoder).matches(currentPassword, accountDto.password());
        verify(passwordEncoder, org.mockito.Mockito.never())
            .encode(org.mockito.ArgumentMatchers.any());
    }

    @Test
    @DisplayName("Should throw DomainException when new password is the same as current password")
    void shouldThrowDomainExceptionWhenNewPasswordIsSameAsCurrentPassword() {
        UUID accountId = UUID.randomUUID();
        String currentPassword = RandomStringUtils.secure().nextAlphanumeric(10);
        String encodedCurrentPassword = RandomStringUtils.secure().nextAlphanumeric(15);

        AccountDto accountDto = AccountDto.builder()
            .accountId(accountId)
            .password(encodedCurrentPassword)
            .build();

        ChangePasswordDto dto = ChangePasswordDto.builder()
            .accountId(accountId)
            .email(RandomEmailUtils.generateValidEmail())
            .currentPassword(currentPassword)
            .newPassword(currentPassword)
            .build();

        when(findAccount.findById(accountId)).thenReturn(accountDto);
        when(passwordEncoder.matches(currentPassword, encodedCurrentPassword)).thenReturn(true);

        assertThatThrownBy(() -> service.changePassword(dto))
            .isInstanceOf(DomainException.class)
            .hasMessageContaining("New password cannot be the same as current password");
        verifyNoInteractions(updateAccount, blocklistService, securityAuditService, emailSender,
            eventPublisher);
    }
}
