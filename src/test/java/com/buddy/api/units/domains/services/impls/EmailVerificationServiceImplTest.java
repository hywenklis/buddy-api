package com.buddy.api.units.domains.services.impls;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doNothing;
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
import com.buddy.api.commons.exceptions.NotFoundException;
import com.buddy.api.domains.account.dtos.AccountDto;
import com.buddy.api.domains.account.email.services.impls.AccountValidatorImpl;
import com.buddy.api.domains.account.email.services.impls.EmailSenderImpl;
import com.buddy.api.domains.account.email.services.impls.EmailVerificationServiceImpl;
import com.buddy.api.domains.account.services.UpdateAccount;
import com.buddy.api.units.UnitTestAbstract;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.cache.Cache;

class EmailVerificationServiceImplTest extends UnitTestAbstract {

    @Mock
    private UpdateAccount updateAccount;

    @Mock
    private CacheInitializer cacheInitializer;

    @Mock
    private RateLimitChecker rateLimitChecker;

    @Mock
    private TokenManager tokenManager;

    @Mock
    private EmailSenderImpl emailSender;

    @Mock
    private AccountValidatorImpl accountValidator;

    @Mock
    private Cache verificationTokenCache;

    @InjectMocks
    private EmailVerificationServiceImpl emailVerificationService;

    private AccountDto unverifiedAccount;
    private AccountDto verifiedAccount;
    private String userEmail;
    private UUID accountId;
    private String token;

    @BeforeEach
    void setUp() {
        unverifiedAccount = AccountBuilder.validAccountDto().isVerified(false).build();
        verifiedAccount = AccountBuilder.validAccountDto().isVerified(true).build();
        userEmail = unverifiedAccount.email().value();
        accountId = unverifiedAccount.accountId();
        token = UUID.randomUUID().toString();

        when(cacheInitializer.initializeVerificationTokenCache())
            .thenReturn(verificationTokenCache);

        emailVerificationService.init();
    }

    @Test
    @DisplayName("Should throw CacheInitializationException if cache initialization fails")
    void should_throw_cache_initialization_exception_if_caches_null() {
        when(cacheInitializer.initializeVerificationTokenCache())
            .thenThrow(new CacheInitializationException("cache", "Required caches not found"));

        assertThatThrownBy(() -> emailVerificationService.init())
            .isInstanceOf(CacheInitializationException.class)
            .hasMessageContaining("Required caches not found");

        verify(cacheInitializer, times(2)).initializeVerificationTokenCache();
    }

    @Nested
    @DisplayName("Tests for requestEmail method")
    class RequestEmailTests {

        @Test
        @DisplayName("Should dispatch verification email successfully")
        void should_dispatch_verification_email_successfully() {
            CountDownLatch latch = new CountDownLatch(1);
            when(tokenManager.generateAndStoreToken(userEmail)).thenReturn(token);
            doAnswer(invocation -> {
                latch.countDown();
                return null;
            }).when(emailSender).dispatchVerificationEmail(accountId, userEmail, token);

            emailVerificationService.requestEmail(unverifiedAccount);

            verify(accountValidator, times(1)).validateAccountNotVerified(unverifiedAccount);
            verify(rateLimitChecker, times(1)).checkRateLimit(userEmail, accountId);
            verify(tokenManager, times(1)).generateAndStoreToken(userEmail);
            verify(emailSender, times(1)).dispatchVerificationEmail(accountId, userEmail, token);
        }

        @Test
        @DisplayName("Should handle email sending failure and evict token")
        void should_handle_email_sending_failure() {
            when(tokenManager.generateAndStoreToken(userEmail)).thenReturn(token);
            doThrow(new RuntimeException("Email service failure"))
                .when(emailSender).dispatchVerificationEmail(accountId, userEmail, token);

            assertThatThrownBy(() -> emailVerificationService.requestEmail(unverifiedAccount))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Email service failure");

            verify(accountValidator, times(1)).validateAccountNotVerified(unverifiedAccount);
            verify(rateLimitChecker, times(1)).checkRateLimit(userEmail, accountId);
            verify(tokenManager, times(1)).generateAndStoreToken(userEmail);
            verify(emailSender, times(1)).dispatchVerificationEmail(accountId, userEmail, token);
        }
    }

    @Nested
    @DisplayName("Tests for confirmEmail method")
    class ConfirmEmailTests {

        @Test
        @DisplayName("Should verify email with a valid token")
        void should_verify_email_with_valid_token() {
            when(tokenManager.validateAndGetTokenEmail(token, accountId)).thenReturn(userEmail);
            doNothing().when(accountValidator)
                .validateTokenMatchesAccount(unverifiedAccount, userEmail, accountId);
            doNothing().when(updateAccount).updateIsVerified(userEmail, true);
            doNothing().when(tokenManager).evictToken(token);

            emailVerificationService.confirmEmail(token, unverifiedAccount);

            verify(tokenManager, times(1)).validateAndGetTokenEmail(token, accountId);
            verify(accountValidator, times(1)).validateTokenMatchesAccount(unverifiedAccount,
                userEmail, accountId);
            verify(updateAccount, times(1)).updateIsVerified(userEmail, true);
            verify(tokenManager, times(1)).evictToken(token);
        }

        @Test
        @DisplayName("Should ignore request if account is already verified")
        void should_ignore_request_if_already_verified() {
            when(tokenManager.validateAndGetTokenEmail(token, accountId)).thenReturn(
                verifiedAccount.email().value());
            doNothing().when(accountValidator)
                .validateTokenMatchesAccount(verifiedAccount, verifiedAccount.email().value(),
                    accountId);
            doNothing().when(tokenManager).evictToken(token);

            emailVerificationService.confirmEmail(token, verifiedAccount);

            verify(tokenManager, times(1)).validateAndGetTokenEmail(token, accountId);
            verify(accountValidator, times(1)).validateTokenMatchesAccount(verifiedAccount,
                verifiedAccount.email().value(), accountId);
            verifyNoInteractions(updateAccount);
            verify(tokenManager, times(1)).evictToken(token);
        }

        @Test
        @DisplayName("Should throw NotFoundException for invalid token")
        void should_throw_not_found_exception_for_invalid_token() {
            when(tokenManager.validateAndGetTokenEmail(token, accountId))
                .thenThrow(
                    new NotFoundException("token", "Invalid or expired verification token."));

            assertThatThrownBy(
                () -> emailVerificationService.confirmEmail(token, unverifiedAccount))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Invalid or expired verification token");

            verify(tokenManager, times(1)).validateAndGetTokenEmail(token, accountId);
            verifyNoInteractions(accountValidator, updateAccount);
        }
    }
}
