package com.buddy.api.units.domains.services.impls;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.buddy.api.builders.account.AccountBuilder;
import com.buddy.api.commons.configurations.properties.EmailProperties;
import com.buddy.api.commons.configurations.properties.RateLimitProperties;
import com.buddy.api.commons.exceptions.AccountAlreadyVerifiedException;
import com.buddy.api.commons.exceptions.AuthenticationException;
import com.buddy.api.commons.exceptions.CacheInitializationException;
import com.buddy.api.commons.exceptions.NotFoundException;
import com.buddy.api.commons.exceptions.TooManyRequestsException;
import com.buddy.api.domains.account.dtos.AccountDto;
import com.buddy.api.domains.account.email.services.EmailTemplateLoaderService;
import com.buddy.api.domains.account.email.services.impls.EmailVerificationServiceImpl;
import com.buddy.api.domains.account.services.UpdateAccount;
import com.buddy.api.integrations.clients.manager.ManagerService;
import com.buddy.api.units.UnitTestAbstract;
import java.time.Duration;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
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
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

class EmailVerificationServiceImplTest extends UnitTestAbstract {

    private static final String VERIFICATION_TOKEN_CACHE_NAME = "emailVerificationToken";
    private static final String RATE_LIMIT_CACHE_NAME = "emailVerificationRateLimit";
    private static final String EMAIL_FROM = "no-reply@buddy.com";
    private static final String EMAIL_SUBJECT = "Verify Your Email";
    private static final String TEMPLATE_PATH = "email-verification.html";
    private static final String VERIFICATION_URL = "http://buddy.app/confirm?token=";

    @Mock
    private UpdateAccount updateAccount;

    @Mock
    private ManagerService managerService;

    @Mock
    private EmailTemplateLoaderService emailTemplateLoader;

    @Mock
    private CacheManager cacheManager;

    @Mock
    private EmailProperties emailProperties;

    @Mock
    private RateLimitProperties rateLimitProperties;

    @Mock
    private EmailProperties.Templates templateProperties;

    @Mock
    private Cache verificationTokenCache;

    @Mock
    private Cache rateLimitCache;

    @Mock
    private RedisTemplate<String, String> redisTemplate;

    @Mock
    private ValueOperations<String, String> valueOperations;

    @Captor
    private ArgumentCaptor<String> tokenCaptor;

    @Captor
    private ArgumentCaptor<String> emailBodyCaptor;

    @InjectMocks
    private EmailVerificationServiceImpl emailVerificationService;

    private AccountDto unverifiedAccount;
    private AccountDto verifiedAccount;
    private String userEmail;
    private String expectedTemplate;

    @BeforeEach
    void setUp() {
        unverifiedAccount = AccountBuilder.validAccountDto().isVerified(false).build();
        verifiedAccount = AccountBuilder.validAccountDto().isVerified(true).build();
        userEmail = unverifiedAccount.email().value();
        expectedTemplate = "Olá, Buddy! Click here: {{url}}";

        when(cacheManager.getCache(VERIFICATION_TOKEN_CACHE_NAME)).thenReturn(
            verificationTokenCache);
        when(cacheManager.getCache(RATE_LIMIT_CACHE_NAME)).thenReturn(rateLimitCache);

        emailVerificationService.init();
    }

    @Nested
    @DisplayName("Tests for requestEmail method")
    class RequestEmailTests {

        @Test
        @DisplayName("Should dispatch verification email successfully")
        void should_dispatch_verification_email_successfully() {
            CountDownLatch latch = new CountDownLatch(1);
            when(emailProperties.templates()).thenReturn(templateProperties);
            when(templateProperties.from()).thenReturn(EMAIL_FROM);
            when(templateProperties.subject()).thenReturn(EMAIL_SUBJECT);
            when(templateProperties.templatePath()).thenReturn(TEMPLATE_PATH);
            when(templateProperties.url()).thenReturn(VERIFICATION_URL);
            when(rateLimitProperties.windowMinutes()).thenReturn(1);
            when(rateLimitProperties.maxAttempts()).thenReturn(3);
            when(redisTemplate.opsForValue()).thenReturn(valueOperations);
            when(emailTemplateLoader.load(TEMPLATE_PATH)).thenReturn(expectedTemplate);
            when(valueOperations.increment(RATE_LIMIT_COUNT + userEmail, 1)).thenReturn(1L);
            when(redisTemplate.expire(RATE_LIMIT_COUNT + userEmail, Duration.ofMinutes(1)))
                .thenReturn(true);
            doAnswer(invocation -> {
                latch.countDown();
                return null;
            }).when(managerService).sendEmailNotification(
                eq(List.of(userEmail)),
                eq(EMAIL_FROM),
                eq(EMAIL_SUBJECT),
                anyString()
            );

            emailVerificationService.requestEmail(unverifiedAccount);

            verify(verificationTokenCache, times(1)).put(tokenCaptor.capture(), eq(userEmail));
            String capturedToken = tokenCaptor.getValue();
            assertThat(capturedToken).isNotNull();
            verify(redisTemplate, times(1)).opsForValue();
            verify(valueOperations, times(1)).increment(RATE_LIMIT_COUNT + userEmail, 1);
            verify(redisTemplate, times(1)).expire(RATE_LIMIT_COUNT + userEmail,
                Duration.ofMinutes(1));
            verify(emailTemplateLoader, times(1)).load(TEMPLATE_PATH);
            verify(managerService, times(1)).sendEmailNotification(
                eq(List.of(userEmail)),
                eq(EMAIL_FROM),
                eq(EMAIL_SUBJECT),
                emailBodyCaptor.capture()
            );
            assertThat(emailBodyCaptor.getValue())
                .contains("Olá, Buddy!")
                .contains(VERIFICATION_URL + capturedToken);
        }

        @Test
        @DisplayName("Should throw AccountAlreadyVerifiedException if account is verified")
        void should_throw_account_already_verified_exception() {
            assertThatThrownBy(() -> emailVerificationService.requestEmail(verifiedAccount))
                .isInstanceOf(AccountAlreadyVerifiedException.class)
                .hasMessageContaining("This account is already verified");

            verifyNoInteractions(verificationTokenCache, redisTemplate, managerService,
                emailTemplateLoader);
        }

        @Test
        @DisplayName("Should throw TooManyRequestsException if rate limit is exceeded")
        void should_throw_too_many_requests_exception() {
            when(redisTemplate.opsForValue()).thenReturn(valueOperations);
            when(valueOperations.increment(RATE_LIMIT_COUNT + userEmail, 1)).thenReturn(4L);

            assertThatThrownBy(() -> emailVerificationService.requestEmail(unverifiedAccount))
                .isInstanceOf(TooManyRequestsException.class)
                .hasMessageContaining("Too many verification requests");

            verify(redisTemplate, times(1)).opsForValue();
            verify(valueOperations, times(1)).increment(RATE_LIMIT_COUNT + userEmail, 1);
            verify(redisTemplate, times(0)).expire(RATE_LIMIT_COUNT + userEmail,
                Duration.ofMinutes(1));
            verifyNoInteractions(verificationTokenCache, managerService, emailTemplateLoader);
        }

        @Test
        @DisplayName("Should evict token if email sending fails")
        void should_evict_token_on_email_sending_failure() {
            CountDownLatch latch = new CountDownLatch(1);
            when(emailProperties.templates()).thenReturn(templateProperties);
            when(templateProperties.from()).thenReturn(EMAIL_FROM);
            when(templateProperties.subject()).thenReturn(EMAIL_SUBJECT);
            when(templateProperties.templatePath()).thenReturn(TEMPLATE_PATH);
            when(templateProperties.url()).thenReturn(VERIFICATION_URL);
            when(rateLimitProperties.windowMinutes()).thenReturn(1);
            when(rateLimitProperties.maxAttempts()).thenReturn(3);
            when(redisTemplate.opsForValue()).thenReturn(valueOperations);
            when(emailTemplateLoader.load(TEMPLATE_PATH)).thenReturn(expectedTemplate);
            when(valueOperations.increment(RATE_LIMIT_COUNT + userEmail, 1)).thenReturn(1L);
            when(redisTemplate.expire(RATE_LIMIT_COUNT + userEmail, Duration.ofMinutes(1)))
                .thenReturn(true);
            doAnswer(invocation -> {
                latch.countDown();
                throw new RuntimeException("Email service failure");
            }).when(managerService).sendEmailNotification(
                eq(List.of(userEmail)),
                eq(EMAIL_FROM),
                eq(EMAIL_SUBJECT),
                anyString()
            );
            doNothing().when(verificationTokenCache).evict(anyString());

            emailVerificationService.requestEmail(unverifiedAccount);

            verify(verificationTokenCache, times(1)).put(tokenCaptor.capture(), eq(userEmail));
            String capturedToken = tokenCaptor.getValue();
            verify(verificationTokenCache, times(1)).evict(capturedToken);
            verify(redisTemplate, times(1)).opsForValue();
            verify(valueOperations, times(1)).increment(RATE_LIMIT_COUNT + userEmail, 1);
            verify(redisTemplate, times(1)).expire(RATE_LIMIT_COUNT + userEmail,
                Duration.ofMinutes(1));
            verify(emailTemplateLoader, times(1)).load(TEMPLATE_PATH);
            verify(managerService, times(1)).sendEmailNotification(
                eq(List.of(userEmail)),
                eq(EMAIL_FROM),
                eq(EMAIL_SUBJECT),
                emailBodyCaptor.capture()
            );
            assertThat(emailBodyCaptor.getValue())
                .contains("Olá, Buddy!")
                .contains(VERIFICATION_URL + capturedToken);
        }

        @Test
        @DisplayName("Should throw CacheInitializationException if caches are null")
        void should_throw_cache_initialization_exception_if_caches_null() {
            when(cacheManager.getCache(VERIFICATION_TOKEN_CACHE_NAME)).thenReturn(null);
            when(cacheManager.getCache(RATE_LIMIT_CACHE_NAME)).thenReturn(null);

            assertThatThrownBy(() -> emailVerificationService.init())
                .isInstanceOf(CacheInitializationException.class)
                .hasMessageContaining("Required caches not found");

            verify(cacheManager, times(2)).getCache(VERIFICATION_TOKEN_CACHE_NAME);
            verify(cacheManager, times(2)).getCache(RATE_LIMIT_CACHE_NAME);
        }
    }

    @Nested
    @DisplayName("Tests for confirmEmail method")
    class ConfirmEmailTests {

        @Test
        @DisplayName("Should verify email with a valid token")
        void should_verify_email_with_valid_token() {
            String token = UUID.randomUUID().toString();
            when(verificationTokenCache.get(token, String.class)).thenReturn(userEmail);
            doNothing().when(updateAccount).updateIsVerified(userEmail, true);
            doNothing().when(verificationTokenCache).evict(token);

            emailVerificationService.confirmEmail(token, unverifiedAccount);

            verify(verificationTokenCache, times(1)).get(token, String.class);
            verify(updateAccount, times(1)).updateIsVerified(userEmail, true);
            verify(verificationTokenCache, times(1)).evict(token);
        }

        @Test
        @DisplayName("Should ignore request if account is already verified")
        void should_ignore_request_if_already_verified() {
            String token = UUID.randomUUID().toString();
            when(verificationTokenCache.get(token, String.class)).thenReturn(
                verifiedAccount.email().value());
            doNothing().when(verificationTokenCache).evict(token);

            emailVerificationService.confirmEmail(token, verifiedAccount);

            verify(verificationTokenCache, times(1)).get(token, String.class);
            verifyNoInteractions(updateAccount);
            verify(verificationTokenCache, times(1)).evict(token);
        }

        @Test
        @DisplayName("Should throw NotFoundException for invalid token")
        void should_throw_not_found_exception_for_invalid_token() {
            String token = UUID.randomUUID().toString();
            when(verificationTokenCache.get(token, String.class)).thenReturn(null);

            assertThatThrownBy(
                () -> emailVerificationService.confirmEmail(token, unverifiedAccount))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Invalid or expired verification token");

            verify(verificationTokenCache, times(1)).get(token, String.class);
            verifyNoInteractions(updateAccount);
            verify(verificationTokenCache, times(0)).evict(token);
        }

        @Test
        @DisplayName("Should throw AuthenticationException for mismatched email")
        void should_throw_authentication_exception_for_mismatched_email() {
            String token = UUID.randomUUID().toString();
            String anotherEmail = "wrong@example.com";
            when(verificationTokenCache.get(token, String.class)).thenReturn(anotherEmail);

            assertThatThrownBy(
                () -> emailVerificationService.confirmEmail(token, unverifiedAccount))
                .isInstanceOf(AuthenticationException.class)
                .hasMessageContaining("Token does not belong to the authenticated user");

            verify(verificationTokenCache, times(1)).get(token, String.class);
            verifyNoInteractions(updateAccount);
            verify(verificationTokenCache, times(0)).evict(token);
        }
    }
}
