package com.buddy.api.units.commons.configurations.cache;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.buddy.api.commons.configurations.cache.CacheInitializer;
import com.buddy.api.commons.exceptions.CacheInitializationException;
import com.buddy.api.units.UnitTestAbstract;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;

class CacheInitializerTest extends UnitTestAbstract {

    private static final String VERIFICATION_TOKEN_CACHE_NAME = "emailVerificationToken";
    private static final String RATE_LIMIT_CACHE_NAME = "emailVerificationRateLimit";

    @Mock
    private CacheManager cacheManager;


    @Mock
    private Cache verificationTokenCache;

    @Mock
    private Cache rateLimitCache;

    @InjectMocks
    private CacheInitializer cacheInitializer;

    @Nested
    @DisplayName("Tests for initializeVerificationTokenCache method")
    class InitializeVerificationTokenCacheTests {


        @Test
        @DisplayName("Should initialize and return verification token cache successfully")
        void should_initialize_and_return_verification_token_cache_successfully() {
            when(cacheManager.getCache(VERIFICATION_TOKEN_CACHE_NAME)).thenReturn(
                verificationTokenCache);
            when(cacheManager.getCache(RATE_LIMIT_CACHE_NAME)).thenReturn(rateLimitCache);

            Cache result = cacheInitializer.initializeVerificationTokenCache();

            assertThat(result).isNotNull();
            assertThat(result).isEqualTo(verificationTokenCache);
            verify(cacheManager).getCache(VERIFICATION_TOKEN_CACHE_NAME);
            verify(cacheManager).getCache(RATE_LIMIT_CACHE_NAME);
        }

        @Test
        @DisplayName("Should throw CacheInitializationException when "
            + "verification token cache is null")
        void should_throw_cache_initialization_exception_when_verification_cache_null() {
            when(cacheManager.getCache(VERIFICATION_TOKEN_CACHE_NAME)).thenReturn(null);
            when(cacheManager.getCache(RATE_LIMIT_CACHE_NAME)).thenReturn(rateLimitCache);

            assertThatThrownBy(() -> cacheInitializer.initializeVerificationTokenCache())
                .isInstanceOf(CacheInitializationException.class)
                .hasMessageContaining(
                    "Required caches not found: "
                        + "emailVerificationToken or emailVerificationRateLimit");

            verify(cacheManager).getCache(VERIFICATION_TOKEN_CACHE_NAME);
            verify(cacheManager).getCache(RATE_LIMIT_CACHE_NAME);
        }

        @Test
        @DisplayName("Should throw CacheInitializationException when rate limit cache is null")
        void should_throw_cache_initialization_exception_when_rate_limit_cache_null() {
            when(cacheManager.getCache(VERIFICATION_TOKEN_CACHE_NAME)).thenReturn(
                verificationTokenCache);
            when(cacheManager.getCache(RATE_LIMIT_CACHE_NAME)).thenReturn(null);

            assertThatThrownBy(() -> cacheInitializer.initializeVerificationTokenCache())
                .isInstanceOf(CacheInitializationException.class)
                .hasMessageContaining(
                    "Required caches not found: "
                        + "emailVerificationToken or emailVerificationRateLimit");

            verify(cacheManager).getCache(VERIFICATION_TOKEN_CACHE_NAME);
            verify(cacheManager).getCache(RATE_LIMIT_CACHE_NAME);
        }

        @Test
        @DisplayName("Should throw CacheInitializationException when both caches are null")
        void should_throw_cache_initialization_exception_when_both_caches_null() {
            when(cacheManager.getCache(VERIFICATION_TOKEN_CACHE_NAME)).thenReturn(null);
            when(cacheManager.getCache(RATE_LIMIT_CACHE_NAME)).thenReturn(null);

            assertThatThrownBy(() -> cacheInitializer.initializeVerificationTokenCache())
                .isInstanceOf(CacheInitializationException.class)
                .hasMessageContaining(
                    "Required caches not found: "
                        + "emailVerificationToken or emailVerificationRateLimit");

            verify(cacheManager).getCache(VERIFICATION_TOKEN_CACHE_NAME);
            verify(cacheManager).getCache(RATE_LIMIT_CACHE_NAME);
        }
    }
}
