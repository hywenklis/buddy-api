package com.buddy.api.units.commons.configurations.cache;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.buddy.api.commons.configurations.cache.CacheInitializer;
import com.buddy.api.commons.configurations.cache.TokenManager;
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
import org.mockito.Mockito;
import org.springframework.cache.Cache;
import org.springframework.data.redis.cache.RedisCache;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisScriptingCommands;
import org.springframework.data.redis.connection.RedisStringCommands;
import org.springframework.data.redis.connection.ReturnType;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

class TokenManagerTest extends UnitTestAbstract {

    @Mock
    private CacheInitializer cacheInitializer;

    @Mock
    private RedisConnectionFactory redisConnectionFactory;

    @Mock
    private Cache verificationTokenCache;

    @InjectMocks
    private TokenManager tokenManager;

    private String userEmail;
    private UUID accountId;
    private String token;

    @BeforeEach
    void setUp() {
        userEmail = RandomEmailUtils.generateValidEmail();
        accountId = UUID.randomUUID();
        token = UUID.randomUUID().toString();

        when(cacheInitializer.initializeVerificationTokenCache()).thenReturn(
            verificationTokenCache);

        tokenManager.init();
    }

    @Nested
    @DisplayName("Tests for TokenManager methods")
    class TokenManagerMethodsTests {

        @Test
        @DisplayName("Should generate and store token")
        void should_generate_and_store_token() {
            String generatedToken = tokenManager.generateAndStoreToken(userEmail);

            assertThat(generatedToken).isNotNull();
            verify(verificationTokenCache, times(1)).put(generatedToken, userEmail);
        }

        @Test
        @DisplayName("Should validate and get email for valid token")
        void should_validate_and_get_email_for_valid_token() {
            when(verificationTokenCache.get(token, String.class)).thenReturn(userEmail);

            String email = tokenManager.validateAndGetTokenEmail(token, accountId);

            assertThat(email).isEqualTo(userEmail);
            verify(verificationTokenCache, times(1)).get(token, String.class);
        }

        @Test
        @DisplayName("Should throw NotFoundException for invalid token")
        void should_throw_not_found_exception_for_invalid_token() {
            when(verificationTokenCache.get(token, String.class)).thenReturn(null);

            assertThatThrownBy(() -> tokenManager.validateAndGetTokenEmail(token, accountId))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Invalid or expired verification token");

            verify(verificationTokenCache, times(1)).get(token, String.class);
        }

        @Test
        @DisplayName("Should evict token")
        void should_evict_token() {
            doNothing().when(verificationTokenCache).evict(token);

            tokenManager.evictToken(token);

            verify(verificationTokenCache, times(1)).evict(token);
        }

        @Test
        @DisplayName("Should consume token from generic cache and evict it")
        void should_consume_token_from_generic_cache() {
            when(verificationTokenCache.get(token, String.class)).thenReturn(userEmail);

            String email = tokenManager.consumeToken(token);

            assertThat(email).isEqualTo(userEmail);
            verify(verificationTokenCache, times(1)).get(token, String.class);
            verify(verificationTokenCache, times(1)).evict(token);
        }

        @Test
        @DisplayName("Should throw NotFoundException when consuming missing token")
        void should_throw_not_found_when_consuming_missing_token() {
            when(verificationTokenCache.get(token, String.class)).thenReturn(null);

            assertThatThrownBy(() -> tokenManager.consumeToken(token))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Invalid or expired verification token");
        }
    }

    @Nested
    @DisplayName("Tests for atomic Redis consumeToken")
    class RedisConsumeTests {

        private RedisCache redisCache;
        private RedisStringCommands redisStringCommands;
        private RedisScriptingCommands redisScriptingCommands;
        private TokenManager redisTokenManager;

        @BeforeEach
        void setUpRedis() {
            final CacheInitializer redisCacheInitializer = mock(CacheInitializer.class);
            redisCache = mock(RedisCache.class);
            when(redisCacheInitializer.initializeVerificationTokenCache()).thenReturn(redisCache);
            Mockito.lenient().when(redisCache.getName()).thenReturn("emailVerificationToken");
            Mockito.lenient().when(redisCache.getCacheConfiguration()).thenReturn(
                RedisCacheConfiguration.defaultCacheConfig()
                    .serializeKeysWith(RedisSerializationContext.SerializationPair
                        .fromSerializer(new StringRedisSerializer()))
                    .serializeValuesWith(RedisSerializationContext.SerializationPair
                        .fromSerializer(new StringRedisSerializer())));

            final RedisConnectionFactory factory = mock(RedisConnectionFactory.class);
            final RedisConnection connection = mock(RedisConnection.class);
            redisStringCommands = mock(RedisStringCommands.class);
            redisScriptingCommands = mock(RedisScriptingCommands.class);
            Mockito.lenient().when(factory.getConnection()).thenReturn(connection);
            Mockito.lenient().when(connection.stringCommands()).thenReturn(redisStringCommands);
            Mockito.lenient().when(connection.scriptingCommands())
                .thenReturn(redisScriptingCommands);

            redisTokenManager = new TokenManager(redisCacheInitializer, factory);
            redisTokenManager.init();
        }

        @Test
        @DisplayName("Should consume token atomically via Redis GETDEL")
        void should_consume_token_via_redis_getdel() {
            byte[] serializedEmail = new StringRedisSerializer().serialize(userEmail);
            when(redisStringCommands.getDel(("emailVerificationToken::" + token).getBytes()))
                .thenReturn(serializedEmail);

            String email = redisTokenManager.consumeToken(token);

            assertThat(email).isEqualTo(userEmail);
            verify(redisStringCommands, times(1))
                .getDel(("emailVerificationToken::" + token).getBytes());
        }

        @Test
        @DisplayName("Should fallback to Lua script when Redis GETDEL is unsupported")
        void should_fallback_to_lua_when_getdel_unsupported() {
            byte[] serializedEmail = new StringRedisSerializer().serialize(userEmail);
            when(redisStringCommands.getDel(any())).thenThrow(new UnsupportedOperationException());
            when(redisScriptingCommands.eval(
                any(byte[].class), eq(ReturnType.VALUE), eq(1), any(byte[].class)))
                .thenReturn(serializedEmail);

            String email = redisTokenManager.consumeToken(token);

            assertThat(email).isEqualTo(userEmail);
            verify(redisScriptingCommands, times(1)).eval(
                any(byte[].class), eq(ReturnType.VALUE), eq(1), any(byte[].class));
        }

        @Test
        @DisplayName("Should throw NotFoundException when Redis GETDEL returns null")
        void should_throw_not_found_when_redis_returns_null() {
            when(redisStringCommands.getDel(("emailVerificationToken::" + token).getBytes()))
                .thenReturn(null);

            assertThatThrownBy(() -> redisTokenManager.consumeToken(token))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Invalid or expired verification token");
        }
    }

    @Test
    @DisplayName("Should initialize cache correctly via CacheInitializer")
    void should_initialize_cache_correctly() {
        verify(cacheInitializer, times(1)).initializeVerificationTokenCache();
        assertThat(tokenManager).hasFieldOrPropertyWithValue("verificationTokenCache",
            verificationTokenCache);
    }

    @Test
    @DisplayName("Should throw CacheInitializationException when cache initialization fails")
    void should_throw_cache_initialization_exception() {
        when(cacheInitializer.initializeVerificationTokenCache())
            .thenThrow(new CacheInitializationException("cache", "Failed to initialize cache"));

        assertThatThrownBy(() -> tokenManager.init())
            .isInstanceOf(CacheInitializationException.class)
            .hasMessageContaining("Failed to initialize cache");

        verify(cacheInitializer, times(2)).initializeVerificationTokenCache();
    }
}
