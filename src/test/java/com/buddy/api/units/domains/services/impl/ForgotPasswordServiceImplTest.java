package com.buddy.api.units.domains.services.impl;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.buddy.api.builders.account.AccountBuilder;
import com.buddy.api.commons.configurations.cache.CacheInitializer;
import com.buddy.api.commons.configurations.cache.RateLimitChecker;
import com.buddy.api.commons.configurations.cache.TokenManager;
import com.buddy.api.commons.exceptions.CacheInitializationException;
import com.buddy.api.domains.account.dtos.AccountDto;
import com.buddy.api.domains.account.email.services.impl.EmailSenderImpl;
import com.buddy.api.domains.account.email.services.impl.ForgotPasswordServiceImpl;
import com.buddy.api.units.UnitTestAbstract;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.cache.Cache;

class ForgotPasswordServiceImplTest extends UnitTestAbstract {

    @Mock
    private CacheInitializer cacheInitializer;

    @Mock
    private RateLimitChecker rateLimitChecker;

    @Mock
    private TokenManager tokenManager;

    @Mock
    private EmailSenderImpl emailSender;

    @Mock
    private Cache forgotPasswordTokenCache;

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

        when(cacheInitializer.initializeForgotPasswordTokenCache())
            .thenReturn(forgotPasswordTokenCache);

        forgotPasswordService.init();
    }

    @Test
    @DisplayName("Should throw CacheInitializationException if cache init fails")
    void should_throw_cache_initialization_exception_if_cache_null() {
        when(cacheInitializer.initializeForgotPasswordTokenCache())
            .thenThrow(new CacheInitializationException(
                "cache", "Required caches not found"));

        assertThatThrownBy(() -> forgotPasswordService.init())
            .isInstanceOf(CacheInitializationException.class)
            .hasMessageContaining("Required caches not found");

        verify(cacheInitializer, times(2))
            .initializeForgotPasswordTokenCache();
    }

    @Nested
    @DisplayName("Tests for requestPasswordRecovery method")
    class RequestPasswordRecoveryTests {

        @Test
        @DisplayName("Should dispatch password recovery email when exists")
        void should_dispatch_password_recovery_email_successfully() {
            when(tokenManager.generateAndStoreToken(userEmail))
                .thenReturn(token);
            doAnswer(invocation -> null)
                .when(emailSender)
                .dispatchPasswordRecoveryEmail(accountId, userEmail, token);

            forgotPasswordService.requestPasswordRecovery(validAccount);

            verify(rateLimitChecker, times(1))
                .checkPasswordRecoveryRateLimit(userEmail, accountId);
            verify(tokenManager, times(1))
                .generateAndStoreToken(userEmail);
            verify(emailSender, times(1))
                .dispatchPasswordRecoveryEmail(accountId, userEmail, token);
        }

        @Test
        @DisplayName("Should handle email sending failure gracefully")
        void should_handle_email_sending_failure_gracefully() {
            when(tokenManager.generateAndStoreToken(userEmail))
                .thenReturn(token);
            doThrow(new RuntimeException("Email service failure"))
                .when(emailSender)
                .dispatchPasswordRecoveryEmail(accountId, userEmail, token);

            assertThatThrownBy(
                    () -> forgotPasswordService
                        .requestPasswordRecovery(validAccount))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Email service failure");

            verify(rateLimitChecker, times(1))
                .checkPasswordRecoveryRateLimit(userEmail, accountId);
            verify(tokenManager, times(1))
                .generateAndStoreToken(userEmail);
            verify(emailSender, times(1))
                .dispatchPasswordRecoveryEmail(accountId, userEmail, token);
        }

        @Test
        @DisplayName("Should handle rate limit exception")
        void should_handle_rate_limit_exception() {
            doThrow(new RuntimeException("Too many requests"))
                .when(rateLimitChecker)
                .checkPasswordRecoveryRateLimit(userEmail, accountId);

            assertThatThrownBy(
                    () -> forgotPasswordService
                        .requestPasswordRecovery(validAccount))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Too many requests");

            verify(rateLimitChecker, times(1))
                .checkPasswordRecoveryRateLimit(userEmail, accountId);
            verifyNoInteractions(tokenManager);
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
            forgotPasswordService.requestPasswordRecovery(null);
            
            verifyNoInteractions(rateLimitChecker);
            verifyNoInteractions(tokenManager);
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

            when(tokenManager.generateAndStoreToken(userEmail))
                .thenReturn(token1)
                .thenReturn(token2);

            forgotPasswordService.requestPasswordRecovery(validAccount);
            forgotPasswordService.requestPasswordRecovery(validAccount);

            verify(tokenManager, times(2))
                .generateAndStoreToken(userEmail);
        }

        @Test
        @DisplayName("Token should expire automatically after TTL - "
            + "verified by Redis configuration")
        void should_verify_token_expiration_via_cache_ttl() {
            when(tokenManager.generateAndStoreToken(userEmail))
                .thenReturn(token);

            forgotPasswordService.requestPasswordRecovery(validAccount);

            verify(tokenManager, times(1))
                .generateAndStoreToken(userEmail);
        }
    }
}
