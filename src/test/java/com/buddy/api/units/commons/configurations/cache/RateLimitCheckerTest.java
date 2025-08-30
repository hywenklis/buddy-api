package com.buddy.api.units.commons.configurations.cache;

import static org.assertj.core.api.AssertionsForClassTypes.assertThatNoException;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.buddy.api.commons.configurations.cache.RateLimitChecker;
import com.buddy.api.commons.configurations.properties.RateLimitProperties;
import com.buddy.api.commons.exceptions.TooManyRequestsException;
import com.buddy.api.units.UnitTestAbstract;
import com.buddy.api.utils.RandomEmailUtils;
import java.time.Duration;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

class RateLimitCheckerTest extends UnitTestAbstract {

    private static final String RATE_LIMIT_COUNT_KEY_PREFIX = "rate-limit:count:";

    @Mock
    private RedisTemplate<String, String> redisTemplate;

    @Mock
    private RateLimitProperties rateLimitProperties;

    @Mock
    private ValueOperations<String, String> valueOperations;

    @InjectMocks
    private RateLimitChecker rateLimitChecker;

    private String email;
    private UUID accountId;
    private String countKey;

    @BeforeEach
    void setUp() {
        email = RandomEmailUtils.generateValidEmail();
        accountId = UUID.randomUUID();
        countKey = RATE_LIMIT_COUNT_KEY_PREFIX + email;

        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
    }

    @Nested
    @DisplayName("Tests for checkRateLimit method")
    class CheckRateLimitTests {

        @Test
        @DisplayName("Should set expiration on first attempt (count == 1)")
        void should_set_expiration_on_first_attempt() {
            when(rateLimitProperties.windowMinutes()).thenReturn(1);
            when(rateLimitProperties.maxAttempts()).thenReturn(3);
            when(valueOperations.increment(countKey, 1)).thenReturn(1L);
            when(redisTemplate.expire(countKey, Duration.ofMinutes(1))).thenReturn(true);

            assertThatNoException().isThrownBy(
                () -> rateLimitChecker.checkRateLimit(email, accountId));

            verify(redisTemplate, times(1)).opsForValue();
            verify(valueOperations, times(1)).increment(countKey, 1);
            verify(redisTemplate, times(1)).expire(countKey, Duration.ofMinutes(1));
        }

        @Test
        @DisplayName("Should throw TooManyRequestsException when rate limit is exceeded")
        void should_throw_too_many_requests_exception_when_limit_exceeded() {
            when(valueOperations.increment(countKey, 1)).thenReturn(4L);
            when(rateLimitProperties.maxAttempts()).thenReturn(3);

            assertThatThrownBy(() -> rateLimitChecker.checkRateLimit(email, accountId))
                .isInstanceOf(TooManyRequestsException.class)
                .hasMessage(
                    "Too many verification requests. Please wait a minute before trying again.");

            verify(redisTemplate, times(1)).opsForValue();
            verify(valueOperations, times(1)).increment(countKey, 1);
            verify(redisTemplate, times(0)).expire(countKey, Duration.ofMinutes(1));
        }

        @Test
        @DisplayName("Should handle null count from Redis")
        void should_handle_null_count_from_redis() {
            when(valueOperations.increment(countKey, 1)).thenReturn(null);

            assertThatNoException().isThrownBy(
                () -> rateLimitChecker.checkRateLimit(email, accountId));

            verify(redisTemplate, times(1)).opsForValue();
            verify(valueOperations, times(1)).increment(countKey, 1);
            verify(redisTemplate, times(0)).expire(countKey, Duration.ofMinutes(1));
        }
    }
}