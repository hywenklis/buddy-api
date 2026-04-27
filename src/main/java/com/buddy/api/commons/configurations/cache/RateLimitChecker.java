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
    private static final String VERIFICATION_OPERATION = "verification";
    private static final String PASSWORD_RECOVERY_OPERATION = "password recovery";
    private static final String VERIFICATION_LIMIT_MESSAGE =
        "Too many verification requests. Please wait a minute before trying again.";
    private static final String PASSWORD_RECOVERY_LIMIT_MESSAGE =
        "Too many password recovery requests. Please wait a minute before trying again.";

    private final RedisTemplate<String, String> redisTemplate;
    private final RateLimitProperties rateLimitProperties;

    public void checkRateLimit(final String email, final UUID accountId) {
        checkRateLimit(
            email,
            accountId,
            VERIFICATION_OPERATION,
            VERIFICATION_LIMIT_MESSAGE
        );
    }

    private void checkRateLimit(final String email,
                                final UUID accountId,
                                final String operation,
                                final String limitMessage
    ) {
        String countKey = buildCountKey(operation, email);
        Long count = redisTemplate.opsForValue().increment(countKey, 1);

        if (count != null && count == 1) {
            redisTemplate.expire(countKey, Duration.ofMinutes(rateLimitProperties.windowMinutes()));
        }

        if (count != null && count > rateLimitProperties.maxAttempts()) {
            log.warn("Rate limit exceeded for {} request for account={}",
                operation,
                accountId
            );

            throw new TooManyRequestsException(limitMessage);
        }
    }

    public void checkPasswordRecoveryRateLimit(final String email, final UUID accountId) {
        checkRateLimit(
            email,
            accountId,
            PASSWORD_RECOVERY_OPERATION,
            PASSWORD_RECOVERY_LIMIT_MESSAGE
        );
    }

    private String buildCountKey(final String operation, final String email) {
        return RATE_LIMIT_COUNT_KEY_PREFIX + operation + ":" + email;
    }
}
