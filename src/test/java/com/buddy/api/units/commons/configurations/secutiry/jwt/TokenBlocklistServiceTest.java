package com.buddy.api.units.commons.configurations.secutiry.jwt;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.buddy.api.commons.configurations.security.jwt.TokenBlocklistService;
import com.buddy.api.units.UnitTestAbstract;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

class TokenBlocklistServiceTest extends UnitTestAbstract {
    private static final String TEST_JWT = "test.jwt";
    private static final String JWT_BLOCKLIST_PREFIX = "jwt:blocklist:";
    private static final String TEST_EMAIL = "test@example.com";
    private static final String JWT_REVOKE_ALL_PREFIX = "jwt:revoke_all:test@example.com";
    private static final int HEX_LENGTH_SINGLE_DIGIT = 1;


    @Mock
    private StringRedisTemplate redisTemplate;

    @Mock
    private ValueOperations<String, String> valueOperations;

    @InjectMocks
    private TokenBlocklistService tokenBlocklistService;

    @Test
    @DisplayName("Should block token when expiration is positive")
    void blockToken_positiveExpiration() {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);

        tokenBlocklistService.blockToken(TEST_JWT, 3600);

        verify(redisTemplate).opsForValue();
        verify(valueOperations).set(JWT_BLOCKLIST_PREFIX + hashToken(TEST_JWT), "blocked",
            Duration.ofSeconds(3600));
    }

    @Test
    @DisplayName("Should not block token when expiration is zero or negative")
    void blockToken_negativeOrZeroExpiration() {
        tokenBlocklistService.blockToken(TEST_JWT, 0);
        tokenBlocklistService.blockToken(TEST_JWT, -10);

        verify(redisTemplate, never()).opsForValue();
    }

    @Test
    @DisplayName("Should return true when token is in blocklist")
    void isBlocked_true() {
        when(redisTemplate.hasKey(JWT_BLOCKLIST_PREFIX + hashToken(TEST_JWT))).thenReturn(
            Boolean.TRUE);

        boolean result = tokenBlocklistService.isBlocked(TEST_JWT);

        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("Should return false when token is not in blocklist")
    void isBlocked_false() {
        when(redisTemplate.hasKey(JWT_BLOCKLIST_PREFIX + hashToken(TEST_JWT))).thenReturn(
            Boolean.FALSE);

        boolean result = tokenBlocklistService.isBlocked(TEST_JWT);

        assertThat(result).isFalse();
    }

    @Test
    @DisplayName("Should return false when hasKey returns null")
    void isBlocked_null() {
        when(redisTemplate.hasKey(JWT_BLOCKLIST_PREFIX + hashToken(TEST_JWT))).thenReturn(null);

        boolean result = tokenBlocklistService.isBlocked(TEST_JWT);

        assertThat(result).isFalse();
    }

    @Test
    @DisplayName("Should block all tokens for a user")
    void revokeAllUserTokens() {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        
        tokenBlocklistService.revokeAllUserTokens(TEST_EMAIL);

        verify(redisTemplate).opsForValue();
        verify(valueOperations).set(
            org.mockito.ArgumentMatchers.eq(JWT_REVOKE_ALL_PREFIX),
            org.mockito.ArgumentMatchers.anyString(),
            org.mockito.ArgumentMatchers.eq(Duration.ofDays(30))
        );
    }

    @Test
    @DisplayName("Should return true when token was issued before revocation timestamp")
    void isUserTokensRevoked_true() {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get(JWT_REVOKE_ALL_PREFIX)).thenReturn("1000");

        boolean result = tokenBlocklistService.isUserTokensRevoked(TEST_EMAIL, 500);

        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("Should return false when token was issued after revocation timestamp")
    void isUserTokensRevoked_false() {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get(JWT_REVOKE_ALL_PREFIX)).thenReturn("1000");

        boolean result = tokenBlocklistService.isUserTokensRevoked(TEST_EMAIL, 2000);

        assertThat(result).isFalse();
    }

    @Test
    @DisplayName("Should return false when no revocation timestamp exists")
    void isUserTokensRevoked_null() {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get(JWT_REVOKE_ALL_PREFIX)).thenReturn(null);

        boolean result = tokenBlocklistService.isUserTokensRevoked(TEST_EMAIL, 500);

        assertThat(result).isFalse();
    }

    @Test
    @DisplayName("Should return true when token issued at same second but before revocation")
    void isUserTokensRevoked_sameLogicalInstant() {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get(JWT_REVOKE_ALL_PREFIX)).thenReturn("1000500");

        boolean result = tokenBlocklistService.isUserTokensRevoked(TEST_EMAIL, 1000000L);

        assertThat(result).isTrue();
    }

    private String hashToken(final String token) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashBytes = digest.digest(token.getBytes(StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder(2 * hashBytes.length);
            for (byte b : hashBytes) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == HEX_LENGTH_SINGLE_DIGIT) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }
}
