package com.buddy.api.units.commons.configurations.cache;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.clearInvocations;
import static org.mockito.Mockito.mock;
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
import org.mockito.Mockito;
import org.springframework.cache.Cache;
import org.springframework.data.redis.cache.RedisCache;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisScriptingCommands;
import org.springframework.data.redis.connection.RedisStringCommands;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

class ForgotPasswordTokenManagerTest extends UnitTestAbstract {

    @Mock
    private CacheInitializer cacheInitializer;

    @Mock
    private RedisCache forgotPasswordTokenCache;

    @Mock
    private RedisConnectionFactory redisConnectionFactory;

    @Mock
    private RedisConnection redisConnection;

    @Mock
    private RedisStringCommands redisStringCommands;

    @Mock
    private RedisScriptingCommands redisScriptingCommands;

    @InjectMocks
    private ForgotPasswordTokenManager forgotPasswordTokenManager;

    private String userEmail;

    @BeforeEach
    void setUp() {
        userEmail = RandomEmailUtils.generateValidEmail();

        when(cacheInitializer.initializeForgotPasswordTokenCache()).thenReturn(
            forgotPasswordTokenCache);
        Mockito.lenient().when(forgotPasswordTokenCache.getName())
            .thenReturn("forgotPasswordToken");
        Mockito.lenient().when(forgotPasswordTokenCache.getCacheConfiguration()).thenReturn(
            RedisCacheConfiguration.defaultCacheConfig()
                .serializeKeysWith(RedisSerializationContext.SerializationPair.fromSerializer(
                    new StringRedisSerializer()))
                .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(
                    new StringRedisSerializer())));
        Mockito.lenient().when(redisConnectionFactory.getConnection()).thenReturn(redisConnection);
        Mockito.lenient().when(redisConnection.stringCommands()).thenReturn(redisStringCommands);

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
        @DisplayName("Should return email by token if it exists in cache")
        void should_return_email_by_token() {
            String token = "some-valid-token";
            Cache.ValueWrapper valueWrapper = () -> userEmail;
            when(forgotPasswordTokenCache.get(token)).thenReturn(valueWrapper);

            String email = forgotPasswordTokenManager.getEmailByToken(token);

            assertThat(email).isEqualTo(userEmail);
            verify(forgotPasswordTokenCache, times(1)).get(token);
        }

        @Test
        @DisplayName("Should return null if token does not exist in cache")
        void should_return_null_if_token_not_found() {
            String token = "invalid-token";
            when(forgotPasswordTokenCache.get(token)).thenReturn(null);

            String email = forgotPasswordTokenManager.getEmailByToken(token);

            assertThat(email).isNull();
            verify(forgotPasswordTokenCache, times(1)).get(token);
        }

        @Test
        @DisplayName("Should return null if token exists in cache but value is null")
        void should_return_null_if_token_value_is_null() {
            String token = "valid-token-null-value";
            Cache.ValueWrapper valueWrapper = () -> null;
            when(forgotPasswordTokenCache.get(token)).thenReturn(valueWrapper);

            String email = forgotPasswordTokenManager.getEmailByToken(token);

            assertThat(email).isNull();
            verify(forgotPasswordTokenCache, times(1)).get(token);
        }

        @Test
        @DisplayName("Should invalidate token from cache")
        void should_invalidate_token() {
            String token = "token-to-invalidate";

            forgotPasswordTokenManager.invalidateToken(token);

            verify(forgotPasswordTokenCache, times(1)).evict(token);
        }

        @Test
        @DisplayName("Should consume token by returning email and invalidating it")
        void should_consume_token_and_return_email() {
            String token = "token-to-consume";
            byte[] serializedEmail = new StringRedisSerializer().serialize(userEmail);
            when(redisStringCommands.getDel(
                ("forgotPasswordToken::" + token).getBytes()))
                .thenReturn(serializedEmail);

            String email = forgotPasswordTokenManager.consumeToken(token);

            assertThat(email).isEqualTo(userEmail);
            verify(redisStringCommands, times(1)).getDel(
                ("forgotPasswordToken::" + token).getBytes());
            verify(forgotPasswordTokenCache, times(0)).get(token);
            verify(forgotPasswordTokenCache, times(0)).evict(token);
        }

        @Test
        @DisplayName("Should return null and not invalidate if token not found when consuming")
        void should_return_null_when_consuming_invalid_token() {
            String token = "invalid-token-to-consume";
            when(redisStringCommands.getDel(
                ("forgotPasswordToken::" + token).getBytes())).thenReturn(null);

            String email = forgotPasswordTokenManager.consumeToken(token);

            assertThat(email).isNull();
            verify(redisStringCommands, times(1)).getDel(
                ("forgotPasswordToken::" + token).getBytes());
            verify(forgotPasswordTokenCache, times(0)).get(token);
            verify(forgotPasswordTokenCache, times(0)).evict(token);
        }
        
        @Test
        @DisplayName("Should return null and not invalidate if token value is null when consuming")
        void should_return_null_and_not_invalidate_if_token_value_is_null_when_consuming() {
            String token = "valid-token-null-value";
            when(redisStringCommands.getDel(
                ("forgotPasswordToken::" + token).getBytes())).thenReturn(null);

            String email = forgotPasswordTokenManager.consumeToken(token);

            assertThat(email).isNull();
            verify(redisStringCommands, times(1)).getDel(
                ("forgotPasswordToken::" + token).getBytes());
            verify(forgotPasswordTokenCache, times(0)).get(token);
            verify(forgotPasswordTokenCache, times(0)).evict(token);
        }

        @Test
        @DisplayName("Should consume token through Lua fallback when GETDEL is unavailable")
        void should_consume_token_through_lua_fallback() {
            String token = "token-to-consume-with-fallback";
            byte[] serializedEmail = new StringRedisSerializer().serialize(userEmail);
            when(redisStringCommands.getDel(any())).thenThrow(new UnsupportedOperationException());
            when(redisConnection.scriptingCommands()).thenReturn(redisScriptingCommands);
            when(redisScriptingCommands.eval(
                any(byte[].class), eq(org.springframework.data.redis.connection.ReturnType.VALUE),
                eq(1), any(byte[].class))).thenReturn(serializedEmail);

            String email = forgotPasswordTokenManager.consumeToken(token);

            assertThat(email).isEqualTo(userEmail);
            verify(redisScriptingCommands).eval(
                any(byte[].class), eq(org.springframework.data.redis.connection.ReturnType.VALUE),
                eq(1), any(byte[].class));
        }

        @Test
        @DisplayName("Should return null when deserialized value is not a String")
        void should_return_null_when_deserialized_value_not_string() {
            String token = "token-with-non-string-value";
            @SuppressWarnings("unchecked")
            RedisSerializationContext.SerializationPair<Object> objectPair =
                mock(RedisSerializationContext.SerializationPair.class);
            when(objectPair.read(any(java.nio.ByteBuffer.class))).thenReturn(12345);

            RedisCacheConfiguration customConfig = RedisCacheConfiguration.defaultCacheConfig()
                .serializeKeysWith(RedisSerializationContext.SerializationPair.fromSerializer(
                    new StringRedisSerializer()))
                .serializeValuesWith(objectPair);

            when(forgotPasswordTokenCache.getCacheConfiguration()).thenReturn(customConfig);
            when(redisStringCommands.getDel(("forgotPasswordToken::" + token).getBytes()))
                .thenReturn(new byte[]{1, 2, 3});

            String email = forgotPasswordTokenManager.consumeToken(token);

            assertThat(email).isNull();
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
        clearInvocations(cacheInitializer);
        when(cacheInitializer.initializeForgotPasswordTokenCache())
            .thenThrow(new CacheInitializationException("cache", "Failed to initialize cache"));

        assertThatThrownBy(() -> forgotPasswordTokenManager.init())
            .isInstanceOf(CacheInitializationException.class)
            .hasMessageContaining("Failed to initialize cache");

        verify(cacheInitializer, times(1)).initializeForgotPasswordTokenCache();
    }
}
