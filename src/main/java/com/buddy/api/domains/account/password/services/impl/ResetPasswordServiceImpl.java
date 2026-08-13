package com.buddy.api.domains.account.password.services.impl;

import com.buddy.api.commons.configurations.cache.ForgotPasswordTokenManager;
import com.buddy.api.commons.configurations.security.jwt.TokenBlocklistService;
import com.buddy.api.commons.exceptions.NotFoundException;
import com.buddy.api.domains.account.password.services.ResetPasswordService;
import com.buddy.api.domains.account.services.UpdateAccount;
import com.buddy.api.web.accounts.requests.ResetPasswordRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class ResetPasswordServiceImpl implements ResetPasswordService {

    private final ForgotPasswordTokenManager tokenManager;
    private final UpdateAccount updateAccount;
    private final TokenBlocklistService blocklistService;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void resetPassword(final ResetPasswordRequest request) {
        String email = tokenManager.getEmailByToken(request.token());
        
        if (email == null) {
            log.warn("Invalid or expired reset token provided");
            throw new NotFoundException("token", "Invalid or expired reset token");
        }

        log.info("Resetting password for user {}", email);
        
        String encodedPassword = passwordEncoder.encode(request.newPassword());
        updateAccount.updatePassword(email, encodedPassword);
        
        tokenManager.invalidateToken(request.token());
        blocklistService.revokeAllUserTokens(email);
        
        log.info("Password successfully reset and all previous tokens revoked for user {}", email);
    }
}
