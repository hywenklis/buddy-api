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
import com.buddy.api.domains.account.password.services.impl.ResetPasswordServiceImpl;
import com.buddy.api.domains.account.services.UpdateAccount;
import com.buddy.api.units.UnitTestAbstract;
import com.buddy.api.utils.RandomEmailUtils;
import com.buddy.api.web.accounts.requests.ResetPasswordRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.security.crypto.password.PasswordEncoder;

class ResetPasswordServiceImplTest extends UnitTestAbstract {

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
        String newPassword = "NewPassword123!";
        String encodedPassword = "encodedPassword";
        
        ResetPasswordRequest request = new ResetPasswordRequest(token, newPassword);

        when(tokenManager.getEmailByToken(token)).thenReturn(email);
        when(passwordEncoder.encode(newPassword)).thenReturn(encodedPassword);

        resetPasswordService.resetPassword(request);

        verify(tokenManager, times(1)).getEmailByToken(token);
        verify(passwordEncoder, times(1)).encode(newPassword);
        verify(updateAccount, times(1)).updatePassword(email, encodedPassword);
        verify(tokenManager, times(1)).invalidateToken(token);
        verify(blocklistService, times(1)).revokeAllUserTokens(email);
    }

    @Test
    @DisplayName("Should throw NotFoundException when token is invalid or expired")
    void should_throw_when_token_invalid() {
        String token = "invalid-token";
        ResetPasswordRequest request = new ResetPasswordRequest(token, "NewPassword123!");

        when(tokenManager.getEmailByToken(token)).thenReturn(null);

        assertThatThrownBy(() -> resetPasswordService.resetPassword(request))
            .isInstanceOf(NotFoundException.class)
            .hasMessage("Invalid or expired reset token");

        verify(tokenManager, times(1)).getEmailByToken(token);
        verify(passwordEncoder, never()).encode(any());
        verify(updateAccount, never()).updatePassword(any(), any());
        verify(tokenManager, never()).invalidateToken(any());
        verify(blocklistService, never()).revokeAllUserTokens(any());
    }
}
