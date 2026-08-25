package com.buddy.api.commons.configurations.security.jwt;

import io.github.resilience4j.retry.annotation.Retry;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Duration;
import java.time.Instant;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class TokenBlocklistService {

    private static final String RESILIENCE_INSTANCE = "tokenBlocklist";
    private static final String KEY_PREFIX = "jwt:blocklist:";
    private static final int HEX_SINGLE_DIGIT_LENGTH = 1;

    private final StringRedisTemplate redisTemplate;

    @Retry(name = RESILIENCE_INSTANCE, fallbackMethod = "fallbackBlockToken")
    public void blockToken(final String token, final long expirationInSeconds) {
        if (expirationInSeconds <= 0) {
            return;
        }
        final String key = KEY_PREFIX + tokenHash(token);
        redisTemplate.opsForValue().set(key, "blocked", Duration.ofSeconds(expirationInSeconds));
        log.debug("Token blocked for {} seconds", expirationInSeconds);
    }

    public void fallbackBlockToken(final String token,
                                   final long expirationInSeconds,
                                   final Throwable ex) {
        log.error("Failed to block token in Redis after retry attempts: {}",
            ex.getMessage(), ex);
    }

    @Retry(name = RESILIENCE_INSTANCE, fallbackMethod = "fallbackIsBlocked")
    public boolean isBlocked(final String token) {
        return Boolean.TRUE.equals(redisTemplate.hasKey(KEY_PREFIX + tokenHash(token)));
    }

    public boolean fallbackIsBlocked(final String token, final Throwable ex) {
        log.error("Failed to check if token is blocked in Redis after retries: {}",
            ex.getMessage(), ex);
        return false;
    }

    @Retry(name = RESILIENCE_INSTANCE, fallbackMethod = "fallbackRevokeAllUserTokens")
    public void revokeAllUserTokens(final String email) {
        final String key = "jwt:revoke_all:" + email;
        long revokeTimestamp = Instant.now().toEpochMilli();
        redisTemplate.opsForValue().set(
            key,
            String.valueOf(revokeTimestamp),
            Duration.ofDays(30)
        );
        log.debug("All tokens revoked for user {} at {}", email, revokeTimestamp);
    }

    public void fallbackRevokeAllUserTokens(final String email, final Throwable ex) {
        log.error("Failed to revoke tokens in Redis for user {} after retries: {}",
            email, ex.getMessage(), ex);
    }

    @Retry(name = RESILIENCE_INSTANCE, fallbackMethod = "fallbackIsUserTokensRevoked")
    public boolean isUserTokensRevoked(final String email, final long issuedAtEpochMilli) {
        final String key = "jwt:revoke_all:" + email;
        String revokedTimestampStr = redisTemplate.opsForValue().get(key);
        if (revokedTimestampStr != null) {
            long revokedTimestamp = Long.parseLong(revokedTimestampStr);
            return issuedAtEpochMilli < revokedTimestamp;
        }
        return false;
    }

    public boolean fallbackIsUserTokensRevoked(final String email,
                                               final long issuedAtEpochMilli,
                                               final Throwable ex) {
        log.error("Failed to check revoked tokens in Redis for user {} after retries: {}",
            email, ex.getMessage(), ex);
        return false;
    }

    @SneakyThrows
    private String tokenHash(final String token) {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] hashBytes = digest.digest(token.getBytes(StandardCharsets.UTF_8));
        StringBuilder hexString = new StringBuilder(2 * hashBytes.length);
        for (byte b : hashBytes) {
            String hex = Integer.toHexString(0xff & b);
            if (hex.length() == HEX_SINGLE_DIGIT_LENGTH) {
                hexString.append('0');
            }
            hexString.append(hex);
        }
        return hexString.toString();
    }
}
