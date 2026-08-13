package com.buddy.api.commons.configurations.security.jwt;

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

    private final StringRedisTemplate redisTemplate;
    private static final String KEY_PREFIX = "jwt:blocklist:";
    private static final int HEX_SINGLE_DIGIT_LENGTH = 1;

    public void blockToken(final String token, final long expirationInSeconds) {
        if (expirationInSeconds <= 0) {
            return;
        }
        final String key = KEY_PREFIX + tokenHash(token);
        redisTemplate.opsForValue().set(key, "blocked", Duration.ofSeconds(expirationInSeconds));
        log.debug("Token blocked for {} seconds", expirationInSeconds);
    }

    public boolean isBlocked(final String token) {
        return Boolean.TRUE.equals(redisTemplate.hasKey(KEY_PREFIX + tokenHash(token)));
    }

    public void revokeAllUserTokens(final String email) {
        final String key = "jwt:revoke_all:" + email;
        redisTemplate.opsForValue().set(
            key,
            String.valueOf(Instant.now().getEpochSecond()),
            Duration.ofDays(30)
        );
        log.debug("All tokens revoked for user {} at {}", email, Instant.now().getEpochSecond());
    }

    public boolean isUserTokensRevoked(final String email, final long issuedAtEpochSecond) {
        final String key = "jwt:revoke_all:" + email;
        String revokedTimestampStr = redisTemplate.opsForValue().get(key);
        if (revokedTimestampStr != null) {
            long revokedTimestamp = Long.parseLong(revokedTimestampStr);
            return issuedAtEpochSecond < revokedTimestamp;
        }
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
