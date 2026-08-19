package com.buddy.api.commons.configurations.cache;

import com.buddy.api.commons.configurations.properties.RateLimitProperties;
import com.buddy.api.commons.exceptions.TooManyRequestsException;
import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.BucketConfiguration;
import io.github.bucket4j.distributed.proxy.ProxyManager;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
public class RateLimitChecker {
    private static final String RATE_LIMIT_COUNT_KEY_PREFIX = "rate-limit:count:";
    private static final String VERIFICATION_OPERATION = "verification";
    private static final String PASSWORD_RECOVERY_OPERATION = "password-recovery";
    private static final String VERIFICATION_LIMIT_MESSAGE =
        "Too many verification requests. Please wait a minute before trying again.";
    private static final String PASSWORD_RECOVERY_LIMIT_MESSAGE =
        "Too many password recovery requests. Please wait a minute before trying again.";

    private final ProxyManager<byte[]> proxyManager;
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
        String key = buildCountKey(operation, email);
        Bucket bucket = proxyManager.builder()
            .build(key.getBytes(StandardCharsets.UTF_8), this::bucketConfiguration);

        if (!bucket.tryConsume(1)) {
            log.warn("Rate limit exceeded for {} request for account={}", operation, accountId);
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

    public void checkPasswordChangeRateLimit(final String email, final UUID accountId) {
        checkRateLimit(
            email,
            accountId,
            "password-change",
            "Too many password change attempts. Please wait a minute before trying again."
        );
    }

    public void checkLoginRateLimit(final String email) {
        checkRateLimit(
            email,
            null,
            "login",
            "Too many login attempts. Please wait a minute before trying again."
        );
    }

    public void checkRegistrationRateLimit(final String email) {
        checkRateLimit(
            email,
            null,
            "registration",
            "Too many registration attempts. Please wait a minute before trying again."
        );
    }

    private BucketConfiguration bucketConfiguration() {
        return BucketConfiguration.builder()
            .addLimit(Bandwidth.builder()
                .capacity(rateLimitProperties.maxAttempts())
                .refillIntervally(rateLimitProperties.maxAttempts(),
                    Duration.ofMinutes(rateLimitProperties.windowMinutes()))
                .build()
            )
            .build();
    }

    private String buildCountKey(final String operation, final String email) {
        return RATE_LIMIT_COUNT_KEY_PREFIX + operation + ":" + email;
    }
}
