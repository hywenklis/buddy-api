package com.buddy.api.units.commons.configurations.cache;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.buddy.api.commons.configurations.cache.CacheInitializer;
import com.buddy.api.commons.configurations.cache.ForgotPasswordTokenManager;
import com.buddy.api.commons.exceptions.CacheInitializationException;
import com.buddy.api.units.UnitTestAbstract;
import com.buddy.api.utils.RandomEmailUtils;
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

    @BeforeEach
    void setUp() {
        userEmail = RandomEmailUtils.generateValidEmail();

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
