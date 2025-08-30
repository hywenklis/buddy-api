package com.buddy.api.commons.configurations.cache;

import com.buddy.api.commons.configurations.properties.RateLimitProperties;
import com.buddy.api.commons.exceptions.TooManyRequestsException;
import java.time.Duration;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
public class RateLimitChecker {
    private static final String RATE_LIMIT_COUNT_KEY_PREFIX = "rate-limit:count:";

    private final RedisTemplate<String, String> redisTemplate;
    private final RateLimitProperties rateLimitProperties;

    public void checkRateLimit(final String email, final UUID accountId) {
        String countKey = RATE_LIMIT_COUNT_KEY_PREFIX + email;
        Long count = redisTemplate.opsForValue().increment(countKey, 1);

        if (count != null && count == 1) {
            redisTemplate.expire(countKey, Duration.ofMinutes(rateLimitProperties.windowMinutes()));
        }

        if (count != null && count > rateLimitProperties.maxAttempts()) {
            log.warn("Rate limit exceeded for email verification request for account={}",
                accountId
            );

            throw new TooManyRequestsException(
                "Too many verification requests. Please wait a minute before trying again.");
        }
    }
}
