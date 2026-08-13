package com.buddy.api.units.domains.services.impl;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.buddy.api.builders.account.AccountBuilder;
import com.buddy.api.commons.configurations.cache.ForgotPasswordTokenManager;
import com.buddy.api.commons.configurations.cache.RateLimitChecker;
import com.buddy.api.commons.exceptions.NotFoundException;
import com.buddy.api.domains.account.dtos.AccountDto;
import com.buddy.api.domains.account.email.services.EmailSender;
import com.buddy.api.domains.account.email.services.impl.ForgotPasswordServiceImpl;
import com.buddy.api.domains.valueobjects.EmailAddress;
import com.buddy.api.units.UnitTestAbstract;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

class ForgotPasswordServiceImplTest extends UnitTestAbstract {

    @Mock
    private RateLimitChecker rateLimitChecker;

    @Mock
    private ForgotPasswordTokenManager forgotPasswordTokenManager;

    @Mock
    private EmailSender emailSender;

    @Mock
    private com.buddy.api.domains.account.services.FindAccount findAccount;

    @InjectMocks
    private ForgotPasswordServiceImpl forgotPasswordService;

    private AccountDto validAccount;
    private String userEmail;
    private UUID accountId;
    private String token;

    @BeforeEach
    void setUp() {
        validAccount = AccountBuilder.validAccountDto().build();
        userEmail = validAccount.email().value();
        accountId = validAccount.accountId();
        token = UUID.randomUUID().toString();
    }

    @Nested
    @DisplayName("Tests for requestPasswordRecovery method")
    class RequestPasswordRecoveryTests {

        @Test
        @DisplayName("Should dispatch password recovery email when exists")
        void should_dispatch_password_recovery_email_successfully() {
            when(findAccount.findByEmail(userEmail)).thenReturn(validAccount);
            when(forgotPasswordTokenManager.generateAndStoreToken(userEmail))
                .thenReturn(token);
            doAnswer(invocation -> null)
                .when(emailSender)
                .dispatchPasswordRecoveryEmail(accountId, userEmail, token);

            forgotPasswordService.requestPasswordRecovery(new EmailAddress(userEmail));

            verify(rateLimitChecker, times(1))
                .checkPasswordRecoveryRateLimit(userEmail, accountId);
            verify(forgotPasswordTokenManager, times(1))
                .generateAndStoreToken(userEmail);
            verify(emailSender, times(1))
                .dispatchPasswordRecoveryEmail(accountId, userEmail, token);
        }

        @Test
        @DisplayName("Should handle email sending failure gracefully")
        void should_handle_email_sending_failure_gracefully() {
            when(findAccount.findByEmail(userEmail)).thenReturn(validAccount);
            when(forgotPasswordTokenManager.generateAndStoreToken(userEmail))
                .thenReturn(token);
            doThrow(new RuntimeException("Email service failure"))
                .when(emailSender)
                .dispatchPasswordRecoveryEmail(accountId, userEmail, token);

            assertThatThrownBy(
                () -> forgotPasswordService
                    .requestPasswordRecovery(new EmailAddress(userEmail)))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Email service failure");

            verify(rateLimitChecker, times(1))
                .checkPasswordRecoveryRateLimit(userEmail, accountId);
            verify(forgotPasswordTokenManager, times(1))
                .generateAndStoreToken(userEmail);
            verify(emailSender, times(1))
                .dispatchPasswordRecoveryEmail(accountId, userEmail, token);
        }

        @Test
        @DisplayName("Should handle rate limit exception")
        void should_handle_rate_limit_exception() {
            when(findAccount.findByEmail(userEmail)).thenReturn(validAccount);
            doThrow(new RuntimeException("Too many requests"))
                .when(rateLimitChecker)
                .checkPasswordRecoveryRateLimit(userEmail, accountId);

            assertThatThrownBy(
                () -> forgotPasswordService
                    .requestPasswordRecovery(new EmailAddress(userEmail)))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Too many requests");

            verify(rateLimitChecker, times(1))
                .checkPasswordRecoveryRateLimit(userEmail, accountId);
            verifyNoInteractions(forgotPasswordTokenManager);
            verifyNoInteractions(emailSender);
        }
    }

    @Nested
    @DisplayName("Tests for non-existent email handling")
    class NonExistentEmailHandlingTests {

        @Test
        @DisplayName("Should silently ignore when account is null - "
            + "enumeration protection")
        void should_silently_ignore_non_existent_email() {
            when(findAccount.findByEmail(userEmail)).thenThrow(
                new NotFoundException("email", "not found"));
            forgotPasswordService.requestPasswordRecovery(new EmailAddress(userEmail));

            verifyNoInteractions(rateLimitChecker);
            verifyNoInteractions(forgotPasswordTokenManager);
            verifyNoInteractions(emailSender);
        }
    }

    @Nested
    @DisplayName("Tests for token management")
    class TokenManagementTests {

        @Test
        @DisplayName("Should generate unique tokens for each request")
        void should_generate_unique_tokens() {
            String token1 = UUID.randomUUID().toString();
            String token2 = UUID.randomUUID().toString();

            when(findAccount.findByEmail(userEmail)).thenReturn(validAccount);
            when(forgotPasswordTokenManager.generateAndStoreToken(userEmail))
                .thenReturn(token1)
                .thenReturn(token2);

            forgotPasswordService.requestPasswordRecovery(new EmailAddress(userEmail));
            forgotPasswordService.requestPasswordRecovery(new EmailAddress(userEmail));

            verify(forgotPasswordTokenManager, times(2))
                .generateAndStoreToken(userEmail);
        }

        @Test
        @DisplayName("Should call generateAndStoreToken for each recovery request")
        void should_call_generate_and_store_token_for_recovery() {
            when(findAccount.findByEmail(userEmail)).thenReturn(validAccount);
            when(forgotPasswordTokenManager.generateAndStoreToken(userEmail))
                .thenReturn(token);

            forgotPasswordService.requestPasswordRecovery(new EmailAddress(userEmail));

            verify(forgotPasswordTokenManager, times(1))
                .generateAndStoreToken(userEmail);
        }
    }
}
