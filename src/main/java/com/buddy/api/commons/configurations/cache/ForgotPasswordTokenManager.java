package com.buddy.api.commons.configurations.cache;

import jakarta.annotation.PostConstruct;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.Cache;
import org.springframework.data.redis.RedisSystemException;
import org.springframework.data.redis.cache.RedisCache;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.ReturnType;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ForgotPasswordTokenManager {
    private static final String GET_AND_DELETE_SCRIPT =
        "local value = redis.call('get', KEYS[1]); "
            + "if value then redis.call('del', KEYS[1]); end; return value";

    private final CacheInitializer cacheInitializer;
    private final RedisConnectionFactory redisConnectionFactory;
    private Cache forgotPasswordTokenCache;

    @PostConstruct
    public void init() {
        this.forgotPasswordTokenCache = cacheInitializer.initializeForgotPasswordTokenCache();
    }

    public String generateAndStoreToken(final String userEmail) {
        String token = UUID.randomUUID().toString();
        forgotPasswordTokenCache.put(token, userEmail);
        return token;
    }

    public String getEmailByToken(final String token) {
        Cache.ValueWrapper valueWrapper = forgotPasswordTokenCache.get(token);
        if (valueWrapper != null && valueWrapper.get() != null) {
            return (String) valueWrapper.get();
        }
        return null;
    }

    public String consumeToken(final String token) {
        RedisCache redisCache = (RedisCache) forgotPasswordTokenCache;
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

    public void invalidateToken(final String token) {
        forgotPasswordTokenCache.evict(token);
    }

    private byte[] serializeKey(
        final org.springframework.data.redis.cache.RedisCacheConfiguration cacheConfiguration,
        final String cacheName,
        final String token
    ) {
        String cacheKey = cacheConfiguration.getKeyPrefixFor(cacheName) + token;
        return toBytes(cacheConfiguration.getKeySerializationPair().write(cacheKey));
    }

    private String deserializeValue(
        final org.springframework.data.redis.cache.RedisCacheConfiguration cacheConfiguration,
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
}
