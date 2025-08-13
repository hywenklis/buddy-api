package com.buddy.api.units.domains.services.impls;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.buddy.api.builders.account.AccountBuilder;
import com.buddy.api.commons.configurations.properties.EmailProperties;
import com.buddy.api.commons.exceptions.AccountAlreadyVerifiedException;
import com.buddy.api.commons.exceptions.AuthenticationException;
import com.buddy.api.commons.exceptions.NotFoundException;
import com.buddy.api.commons.exceptions.TooManyRequestsException;
import com.buddy.api.domains.account.dtos.AccountDto;
import com.buddy.api.domains.account.email.services.impls.EmailVerificationServiceImpl;
import com.buddy.api.domains.account.services.UpdateAccount;
import com.buddy.api.integrations.clients.notification.EmailNotificationService;
import com.buddy.api.units.UnitTestAbstract;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;

class EmailVerificationServiceImplTest extends UnitTestAbstract {

    @Mock
    private UpdateAccount updateAccount;
    @Mock
    private EmailNotificationService emailNotificationService;
    @Mock
    private CacheManager cacheManager;
    @Mock
    private EmailProperties emailProperties;
    @Mock
    private EmailProperties.Templates templateProperties;
    @Mock
    private Cache verificationTokenCache;
    @Mock
    private Cache rateLimitCache;

    @InjectMocks
    private EmailVerificationServiceImpl emailVerificationService;

    @Captor
    private ArgumentCaptor<String> tokenCaptor;

    @Captor
    private ArgumentCaptor<String> emailBodyCaptor;

    private AccountDto unverifiedAccount;
    private AccountDto verifiedAccount;
    private String userEmail;

    @BeforeEach
    void setUp() {
        when(cacheManager.getCache(VERIFICATION_TOKEN_CACHE_NAME))
            .thenReturn(verificationTokenCache);

        when(cacheManager.getCache(RATE_LIMIT_CACHE_NAME)).thenReturn(rateLimitCache);
        emailVerificationService.init();

        this.unverifiedAccount = AccountBuilder.validAccountDto().isVerified(false).build();
        this.verifiedAccount = AccountBuilder.validAccountDto().isVerified(true).build();
        this.userEmail = unverifiedAccount.email().value();
    }

    @Nested
    @DisplayName("Tests for requestEmail method")
    class RequestEmailTests {

        @Test
        @DisplayName("Should send verification email for a valid account")
        void requestEmail_whenAccountIsValid_shouldSendVerificationEmail() {
            setupEmailProperties();
            when(rateLimitCache.get(userEmail)).thenReturn(null);

            emailVerificationService.requestEmail(unverifiedAccount);

            verify(verificationTokenCache).put(tokenCaptor.capture(), eq(userEmail));
            String generatedToken = tokenCaptor.getValue();
            assertThat(generatedToken).isNotNull();

            verify(rateLimitCache, times(1)).put(userEmail, "rate-limited");
            verify(emailNotificationService, times(1)).sendEmail(
                eq(List.of(userEmail)),
                eq("Confirm your email"),
                emailBodyCaptor.capture()
            );
            assertThat(emailBodyCaptor.getValue()).contains(
                "http://buddy.app/confirm?token=" + generatedToken);
        }

        @Test
        @DisplayName("Should throw AccountAlreadyVerifiedException if account is already verified")
        void requestEmail_whenAccountIsAlreadyVerified_throwAccountAlreadyVerifiedException() {
            assertThatThrownBy(() -> emailVerificationService.requestEmail(verifiedAccount))
                .isInstanceOf(AccountAlreadyVerifiedException.class)
                .hasMessage("This account is already verified.");

            verifyNoInteractions(verificationTokenCache, rateLimitCache, emailNotificationService);
        }

        @Test
        @DisplayName("Should throw TooManyRequestsException if user is rate-limited")
        void requestEmail_whenUserIsRateLimited_shouldThrowTooManyRequestsException() {
            when(rateLimitCache.get(userEmail)).thenReturn(mock(Cache.ValueWrapper.class));

            assertThatThrownBy(() -> emailVerificationService.requestEmail(unverifiedAccount))
                .isInstanceOf(TooManyRequestsException.class)
                .hasMessageContaining("You have requested a verification email recently.");

            verifyNoInteractions(verificationTokenCache, emailNotificationService);
        }
    }

    @Nested
    @DisplayName("Tests for confirmEmail method")
    class ConfirmEmailTests {

        @Test
        @DisplayName("Should verify email with a valid token")
        void confirmEmail_withValidToken_shouldSucceed() {
            setupValidTokenForEmail(userEmail);

            emailVerificationService.confirmEmail(TOKEN, unverifiedAccount);

            verify(updateAccount, times(1)).updateIsVerified(userEmail, true);
            verify(verificationTokenCache, times(1)).evict(TOKEN);
        }

        @Test
        @DisplayName("Should ignore request if account is already verified")
        void confirmEmail_whenAccountIsAlreadyVerified_shouldDoNothing() {
            setupValidTokenForEmail(verifiedAccount.email().value());

            emailVerificationService.confirmEmail(TOKEN, verifiedAccount);

            verifyNoInteractions(updateAccount);
            verify(verificationTokenCache, times(1)).evict(TOKEN);
        }

        @Test
        @DisplayName("Should throw NotFoundException for a non-existent token")
        void confirmEmail_withInvalidToken_shouldThrowNotFoundException() {
            when(verificationTokenCache.get(TOKEN)).thenReturn(null);

            assertNotFoundException(unverifiedAccount);
        }

        @Test
        @DisplayName("Should throw NotFoundException when token value is null")
        void confirmEmail_whenTokenValueIsNull_shouldThrowNotFoundException() {
            Cache.ValueWrapper valueWrapper = mock(Cache.ValueWrapper.class);
            when(valueWrapper.get()).thenReturn(null);
            when(verificationTokenCache.get(TOKEN)).thenReturn(valueWrapper);

            assertNotFoundException(unverifiedAccount);
        }

        @Test
        @DisplayName("Should throw AuthenticationException when token to another user")
        void confirmEmail_whenTokenBelongsToAnotherUser_shouldThrowAuthenticationException() {
            Cache.ValueWrapper valueWrapper = mock(Cache.ValueWrapper.class);
            when(valueWrapper.get()).thenReturn("another.user@example.com");
            when(verificationTokenCache.get(TOKEN)).thenReturn(valueWrapper);

            assertThatThrownBy(
                () -> emailVerificationService.confirmEmail(TOKEN, unverifiedAccount))
                .isInstanceOf(AuthenticationException.class)
                .hasMessage("Token does not belong to the authenticated user");

            verify(updateAccount, never()).updateIsVerified(userEmail, true);
            verify(verificationTokenCache, never()).evict(TOKEN);
        }

        private void assertNotFoundException(final AccountDto account) {
            assertThatThrownBy(() ->
                emailVerificationService.confirmEmail(UnitTestAbstract.TOKEN, account))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("Invalid or expired verification token.");

            verifyNoInteractions(updateAccount);
        }
    }

    private void setupValidTokenForEmail(final String email) {
        Cache.ValueWrapper valueWrapper = mock(Cache.ValueWrapper.class);
        when(valueWrapper.get()).thenReturn(email);
        when(verificationTokenCache.get(TOKEN)).thenReturn(valueWrapper);
    }

    private void setupEmailProperties() {
        when(emailProperties.templates()).thenReturn(templateProperties);
        when(templateProperties.url()).thenReturn("http://buddy.app/confirm?token=");
        when(templateProperties.subject()).thenReturn("Confirm your email");
        when(templateProperties.verification()).thenReturn("Click here: %s");
    }
}
