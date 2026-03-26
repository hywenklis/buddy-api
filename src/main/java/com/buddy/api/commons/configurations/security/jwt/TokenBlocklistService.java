package com.buddy.api.commons.configurations.security.jwt;

import java.time.Duration;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class TokenBlocklistService {

    private final StringRedisTemplate redisTemplate;
    private static final String KEY_PREFIX = "jwt:blocklist:";

    public void blockToken(final String token, final long expirationInSeconds) {
        if (expirationInSeconds <= 0) {
            return;
        }
        final String key = KEY_PREFIX + token;
        redisTemplate.opsForValue().set(key, "blocked", Duration.ofSeconds(expirationInSeconds));
        log.debug("Token blocked for {} seconds", expirationInSeconds);
    }

    public boolean isBlocked(final String token) {
        return Boolean.TRUE.equals(redisTemplate.hasKey(KEY_PREFIX + token));
    }
}
