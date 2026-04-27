package com.buddy.api.units.commons.configurations.cache;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.buddy.api.commons.configurations.cache.CacheInitializer;
import com.buddy.api.commons.configurations.cache.ForgotPasswordTokenManager;
import com.buddy.api.commons.exceptions.CacheInitializationException;
import com.buddy.api.commons.exceptions.NotFoundException;
import com.buddy.api.units.UnitTestAbstract;
import com.buddy.api.utils.RandomEmailUtils;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.cache.Cache;

class ForgotPasswordTokenManagerTest extends UnitTestAbstract {

    @Mock
    private CacheInitializer cacheInitializer;

    @Mock
    private Cache forgotPasswordTokenCache;

    @InjectMocks
    private ForgotPasswordTokenManager forgotPasswordTokenManager;

    private String userEmail;
    private UUID accountId;
    private String token;

    @BeforeEach
    void setUp() {
        userEmail = RandomEmailUtils.generateValidEmail();
        accountId = UUID.randomUUID();
        token = UUID.randomUUID().toString();

        when(cacheInitializer.initializeForgotPasswordTokenCache()).thenReturn(
            forgotPasswordTokenCache);

        forgotPasswordTokenManager.init();
    }

    @Nested
    @DisplayName("Tests for ForgotPasswordTokenManager methods")
    class ForgotPasswordTokenManagerMethodsTests {

        @Test
        @DisplayName("Should generate and store token in forgot-password cache")
        void should_generate_and_store_token() {
            String generatedToken = forgotPasswordTokenManager.generateAndStoreToken(userEmail);

            assertThat(generatedToken).isNotNull();
            verify(forgotPasswordTokenCache, times(1)).put(generatedToken, userEmail);
        }

        @Test
        @DisplayName("Should validate and get email for valid token")
        void should_validate_and_get_email_for_valid_token() {
            when(forgotPasswordTokenCache.get(token, String.class)).thenReturn(userEmail);

            String email = forgotPasswordTokenManager.validateAndGetTokenEmail(token, accountId);

            assertThat(email).isEqualTo(userEmail);
            verify(forgotPasswordTokenCache, times(1)).get(token, String.class);
        }

        @Test
        @DisplayName("Should throw NotFoundException for invalid token")
        void should_throw_not_found_exception_for_invalid_token() {
            when(forgotPasswordTokenCache.get(token, String.class)).thenReturn(null);

            assertThatThrownBy(
                    () -> forgotPasswordTokenManager.validateAndGetTokenEmail(token, accountId))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Invalid or expired forgot-password token");

            verify(forgotPasswordTokenCache, times(1)).get(token, String.class);
        }

        @Test
        @DisplayName("Should evict token from forgot-password cache")
        void should_evict_token() {
            doNothing().when(forgotPasswordTokenCache).evict(token);

            forgotPasswordTokenManager.evictToken(token);

            verify(forgotPasswordTokenCache, times(1)).evict(token);
        }
    }

    @Test
    @DisplayName("Should initialize forgot-password cache correctly via CacheInitializer")
    void should_initialize_cache_correctly() {
        verify(cacheInitializer, times(1)).initializeForgotPasswordTokenCache();
        assertThat(forgotPasswordTokenManager).hasFieldOrPropertyWithValue(
            "forgotPasswordTokenCache", forgotPasswordTokenCache);
    }

    @Test
    @DisplayName("Should throw CacheInitializationException when cache initialization fails")
    void should_throw_cache_initialization_exception() {
        when(cacheInitializer.initializeForgotPasswordTokenCache())
            .thenThrow(new CacheInitializationException("cache", "Failed to initialize cache"));

        assertThatThrownBy(() -> forgotPasswordTokenManager.init())
            .isInstanceOf(CacheInitializationException.class)
            .hasMessageContaining("Failed to initialize cache");

        verify(cacheInitializer, times(2)).initializeForgotPasswordTokenCache();
    }
}
