package com.buddy.api.commons.configurations.cache;

import com.buddy.api.commons.exceptions.NotFoundException;
import jakarta.annotation.PostConstruct;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.Cache;
import org.springframework.data.redis.RedisSystemException;
import org.springframework.data.redis.cache.RedisCache;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.ReturnType;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
public class TokenManager {

    private static final String GET_AND_DELETE_SCRIPT =
        "local value = redis.call('get', KEYS[1]); "
            + "if value then redis.call('del', KEYS[1]); end; return value";
    private static final int TOKEN_PREFIX_PREVIEW_LENGTH = 8;

    private final CacheInitializer cacheInitializer;
    private final RedisConnectionFactory redisConnectionFactory;
    private Cache verificationTokenCache;

    @PostConstruct
    public void init() {
        this.verificationTokenCache = cacheInitializer.initializeVerificationTokenCache();
    }

    public String generateAndStoreToken(final String userEmail) {
        String token = UUID.randomUUID().toString();
        verificationTokenCache.put(token, userEmail);
        return token;
    }

    public String validateAndGetTokenEmail(final String token, final UUID accountId) {
        String email = verificationTokenCache.get(token, String.class);
        if (email == null) {
            log.warn("Invalid or expired token for account={}. Token: {}",
                accountId, tokenPreview(token));
            throw new NotFoundException("token", "Invalid or expired verification token.");
        }
        return email;
    }

    public String consumeToken(final String token) {
        String email = consumeFromStore(token);
        if (email == null || email.isBlank()) {
            log.warn("Invalid or expired verification token: {}", tokenPreview(token));
            throw new NotFoundException("token", "Invalid or expired verification token.");
        }
        return email;
    }

    public void evictToken(final String token) {
        verificationTokenCache.evict(token);
    }

    private String consumeFromStore(final String token) {
        if (verificationTokenCache instanceof RedisCache redisCache) {
            var cacheConfiguration = redisCache.getCacheConfiguration();
            byte[] key = serializeKey(cacheConfiguration, redisCache.getName(), token);
            byte[] value;

            try (RedisConnection connection = redisConnectionFactory.getConnection()) {
                try {
                    value = connection.stringCommands().getDel(key);
                } catch (UnsupportedOperationException | RedisSystemException exception) {
                    value = connection.scriptingCommands().eval(
                        GET_AND_DELETE_SCRIPT.getBytes(StandardCharsets.UTF_8),
                        ReturnType.VALUE,
                        1,
                        key
                    );
                }
            }

            return deserializeValue(cacheConfiguration, value);
        }

        synchronized (this) {
            String email = verificationTokenCache.get(token, String.class);
            if (email != null) {
                verificationTokenCache.evict(token);
            }
            return email;
        }
    }

    private byte[] serializeKey(
        final RedisCacheConfiguration cacheConfiguration,
        final String cacheName,
        final String token
    ) {
        String cacheKey = cacheConfiguration.getKeyPrefixFor(cacheName) + token;
        return toBytes(cacheConfiguration.getKeySerializationPair().write(cacheKey));
    }

    private String deserializeValue(
        final RedisCacheConfiguration cacheConfiguration,
        final byte[] value
    ) {
        if (value == null) {
            return null;
        }
        Object deserialized = cacheConfiguration.getValueSerializationPair()
            .read(ByteBuffer.wrap(value));
        return deserialized instanceof String email ? email : null;
    }

    private byte[] toBytes(final ByteBuffer buffer) {
        byte[] bytes = new byte[buffer.remaining()];
        buffer.get(bytes);
        return bytes;
    }

    private String tokenPreview(final String token) {
        if (token == null) {
            return "null";
        }
        return token.substring(0, Math.min(token.length(), TOKEN_PREFIX_PREVIEW_LENGTH)) + "...";
    }
}
