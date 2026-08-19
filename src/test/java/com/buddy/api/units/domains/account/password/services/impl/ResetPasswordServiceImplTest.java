package com.buddy.api.units.domains.account.password.services.impl;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.buddy.api.commons.configurations.cache.ForgotPasswordTokenManager;
import com.buddy.api.commons.configurations.security.jwt.TokenBlocklistService;
import com.buddy.api.commons.exceptions.NotFoundException;
import com.buddy.api.domains.account.password.dtos.ResetPasswordDto;
import com.buddy.api.domains.account.password.services.impl.ResetPasswordServiceImpl;
import com.buddy.api.domains.account.services.UpdateAccount;
import com.buddy.api.domains.valueobjects.EmailAddress;
import com.buddy.api.units.UnitTestAbstract;
import com.buddy.api.utils.RandomEmailUtils;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.security.crypto.password.PasswordEncoder;

class ResetPasswordServiceImplTest extends UnitTestAbstract {
    private static final String NEW_PASSWORD = "NewPassword123!";

    @Mock
    private ForgotPasswordTokenManager tokenManager;

    @Mock
    private UpdateAccount updateAccount;

    @Mock
    private TokenBlocklistService blocklistService;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private ResetPasswordServiceImpl resetPasswordService;

    @Test
    @DisplayName("Should successfully reset password and revoke tokens")
    void should_reset_password_successfully() {
        String token = "valid-token";
        String email = RandomEmailUtils.generateValidEmail();
        String newPassword = NEW_PASSWORD;
        String encodedPassword = "encodedPassword";
        
        ResetPasswordDto request = new ResetPasswordDto(token, newPassword);

        when(tokenManager.consumeToken(token)).thenReturn(email);
        when(passwordEncoder.encode(newPassword)).thenReturn(encodedPassword);

        resetPasswordService.resetPassword(request);

        verify(tokenManager, times(1)).consumeToken(token);
        verify(passwordEncoder, times(1)).encode(newPassword);
        verify(updateAccount, times(1)).updatePassword(new EmailAddress(email), encodedPassword);
        verify(blocklistService, times(1)).revokeAllUserTokens(email);
    }

    @Test
    @DisplayName("Should throw NotFoundException when token is invalid or expired")
    void should_throw_when_token_invalid() {
        String token = "invalid-token";
        ResetPasswordDto request = new ResetPasswordDto(token, NEW_PASSWORD);

        when(tokenManager.consumeToken(token)).thenReturn(null);

        assertThatThrownBy(() -> resetPasswordService.resetPassword(request))
            .isInstanceOf(NotFoundException.class)
            .hasMessage("Invalid or expired reset token");

        verify(tokenManager, times(1)).consumeToken(token);
        verify(passwordEncoder, never()).encode(any());
        verify(updateAccount, never()).updatePassword(any(), any());
        verify(blocklistService, never()).revokeAllUserTokens(any());
    }

    @Test
    @DisplayName("Should propagate exception when update password fails")
    void should_propagate_exception_when_update_password_fails() {
        String token = "valid-token";
        String email = RandomEmailUtils.generateValidEmail();
        String newPassword = NEW_PASSWORD;
        String encodedPassword = "encodedPassword";
        
        ResetPasswordDto request = new ResetPasswordDto(token, newPassword);

        when(tokenManager.consumeToken(token)).thenReturn(email);
        when(passwordEncoder.encode(newPassword)).thenReturn(encodedPassword);
        
        RuntimeException expectedException = new RuntimeException("Database error");
        org.mockito.Mockito.doThrow(expectedException)
            .when(updateAccount).updatePassword(new EmailAddress(email), encodedPassword);

        assertThatThrownBy(() -> resetPasswordService.resetPassword(request))
            .isInstanceOf(RuntimeException.class)
            .hasMessage("Database error");

        verify(tokenManager, times(1)).consumeToken(token);
        verify(passwordEncoder, times(1)).encode(newPassword);
        verify(updateAccount, times(1)).updatePassword(new EmailAddress(email), encodedPassword);
        verify(blocklistService, never()).revokeAllUserTokens(any());
    }

    @Test
    @DisplayName("Should propagate exception when revoke tokens fails")
    void should_propagate_exception_when_revoke_tokens_fails() {
        String token = "valid-token";
        String email = RandomEmailUtils.generateValidEmail();
        String newPassword = NEW_PASSWORD;
        String encodedPassword = "encodedPassword";
        
        ResetPasswordDto request = new ResetPasswordDto(token, newPassword);

        when(tokenManager.consumeToken(token)).thenReturn(email);
        when(passwordEncoder.encode(newPassword)).thenReturn(encodedPassword);
        
        RuntimeException expectedException = new RuntimeException("Redis error");
        org.mockito.Mockito.doThrow(expectedException)
            .when(blocklistService).revokeAllUserTokens(email);

        assertThatThrownBy(() -> resetPasswordService.resetPassword(request))
            .isInstanceOf(RuntimeException.class)
            .hasMessage("Redis error");

        verify(tokenManager, times(1)).consumeToken(token);
        verify(passwordEncoder, times(1)).encode(newPassword);
        verify(updateAccount, times(1)).updatePassword(new EmailAddress(email), encodedPassword);
        verify(blocklistService, times(1)).revokeAllUserTokens(email);
    }

}
