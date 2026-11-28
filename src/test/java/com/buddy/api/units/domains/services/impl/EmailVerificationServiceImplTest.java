package com.buddy.api.units.domains.services.impl;

import static org.assertj.core.api.Assertions.assertThatCode;
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
import com.buddy.api.commons.configurations.cache.TokenManager;
import com.buddy.api.commons.exceptions.AccountAlreadyVerifiedException;
import com.buddy.api.commons.exceptions.AccountUnavailableException;
import com.buddy.api.commons.exceptions.CacheInitializationException;
import com.buddy.api.commons.exceptions.NotFoundException;
import com.buddy.api.domains.account.dtos.AccountDto;
import com.buddy.api.domains.account.email.services.impl.AccountValidatorImpl;
import com.buddy.api.domains.account.email.services.impl.EmailSenderImpl;
import com.buddy.api.domains.account.email.services.impl.EmailVerificationServiceImpl;
import com.buddy.api.domains.account.services.FindAccount;
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
    private FindAccount findAccount;

    @Mock
    private UpdateAccount updateAccount;

    @Mock
    private CacheInitializer cacheInitializer;

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
            when(findAccount.findByEmail(userEmail)).thenReturn(unverifiedAccount);
            when(tokenManager.generateAndStoreToken(userEmail)).thenReturn(token);
            doAnswer(invocation -> {
                latch.countDown();
                return null;
            }).when(emailSender).dispatchVerificationEmail(accountId, userEmail, token);

            emailVerificationService.requestEmail(unverifiedAccount.email());

            verify(findAccount, times(1)).findByEmail(userEmail);
            verify(accountValidator, times(1)).validateAccountNotVerified(unverifiedAccount);
            verify(tokenManager, times(1)).generateAndStoreToken(userEmail);
            verify(emailSender, times(1)).dispatchVerificationEmail(accountId, userEmail, token);
        }

        @Test
        @DisplayName("Should silently ignore when account is not found (anti-enumeration)")
        void should_ignore_when_account_not_found() {
            when(findAccount.findByEmail(userEmail))
                .thenThrow(new NotFoundException("email", "Account not found"));

            assertThatCode(() -> emailVerificationService.requestEmail(unverifiedAccount.email()))
                .doesNotThrowAnyException();

            verifyNoInteractions(accountValidator, tokenManager, emailSender);
        }

        @Test
        @DisplayName("Should silently ignore when account is unavailable (anti-enumeration)")
        void should_ignore_when_account_unavailable() {
            when(findAccount.findByEmail(userEmail))
                .thenThrow(new AccountUnavailableException("email", "Account unavailable"));

            assertThatCode(() -> emailVerificationService.requestEmail(unverifiedAccount.email()))
                .doesNotThrowAnyException();

            verifyNoInteractions(accountValidator, tokenManager, emailSender);
        }

        @Test
        @DisplayName("Should silently ignore when account is already verified (anti-enumeration)")
        void should_ignore_when_account_already_verified() {
            when(findAccount.findByEmail(verifiedAccount.email().value()))
                .thenReturn(verifiedAccount);
            doThrow(new AccountAlreadyVerifiedException("account.status", "Already verified"))
                .when(accountValidator).validateAccountNotVerified(verifiedAccount);

            assertThatCode(() -> emailVerificationService.requestEmail(verifiedAccount.email()))
                .doesNotThrowAnyException();

            verifyNoInteractions(tokenManager, emailSender);
        }

        @Test
        @DisplayName("Should propagate email sending failure")
        void should_handle_email_sending_failure() {
            when(findAccount.findByEmail(userEmail)).thenReturn(unverifiedAccount);
            when(tokenManager.generateAndStoreToken(userEmail)).thenReturn(token);
            doThrow(new RuntimeException("Email service failure"))
                .when(emailSender).dispatchVerificationEmail(accountId, userEmail, token);

            assertThatThrownBy(() ->
                emailVerificationService.requestEmail(unverifiedAccount.email()))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Email service failure");

            verify(accountValidator, times(1)).validateAccountNotVerified(unverifiedAccount);
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
            when(tokenManager.consumeToken(token)).thenReturn(userEmail);
            when(findAccount.findByEmail(userEmail)).thenReturn(unverifiedAccount);
            doNothing().when(updateAccount).updateIsVerified(userEmail, true);

            emailVerificationService.confirmEmail(token);

            verify(tokenManager, times(1)).consumeToken(token);
            verify(findAccount, times(1)).findByEmail(userEmail);
            verify(updateAccount, times(1)).updateIsVerified(userEmail, true);
        }

        @Test
        @DisplayName("Should ignore update idempotently if account is already verified")
        void should_ignore_request_if_already_verified() {
            String verifiedEmail = verifiedAccount.email().value();
            when(tokenManager.consumeToken(token)).thenReturn(verifiedEmail);
            when(findAccount.findByEmail(verifiedEmail)).thenReturn(verifiedAccount);

            emailVerificationService.confirmEmail(token);

            verify(tokenManager, times(1)).consumeToken(token);
            verify(findAccount, times(1)).findByEmail(verifiedEmail);
            verifyNoInteractions(updateAccount);
        }

        @Test
        @DisplayName("Should throw NotFoundException for invalid token")
        void should_throw_not_found_exception_for_invalid_token() {
            when(tokenManager.consumeToken(token))
                .thenThrow(
                    new NotFoundException("token", "Invalid or expired verification token."));

            assertThatThrownBy(() -> emailVerificationService.confirmEmail(token))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Invalid or expired verification token");

            verify(tokenManager, times(1)).consumeToken(token);
            verifyNoInteractions(findAccount, updateAccount);
        }
    }
}
