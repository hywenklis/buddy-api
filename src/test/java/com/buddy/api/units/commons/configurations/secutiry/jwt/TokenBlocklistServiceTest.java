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

        tokenBlocklistService.blockToken("test.jwt", 3600);

        verify(redisTemplate).opsForValue();
        verify(valueOperations).set("jwt:blocklist:" + hashToken("test.jwt"), "blocked",
            Duration.ofSeconds(3600));
    }

    @Test
    @DisplayName("Should not block token when expiration is zero or negative")
    void blockToken_negativeOrZeroExpiration() {
        tokenBlocklistService.blockToken("test.jwt", 0);
        tokenBlocklistService.blockToken("test.jwt", -10);

        verify(redisTemplate, never()).opsForValue();
    }

    @Test
    @DisplayName("Should return true when token is in blocklist")
    void isBlocked_true() {
        when(redisTemplate.hasKey("jwt:blocklist:" + hashToken("test.jwt"))).thenReturn(
            Boolean.TRUE);

        boolean result = tokenBlocklistService.isBlocked("test.jwt");

        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("Should return false when token is not in blocklist")
    void isBlocked_false() {
        when(redisTemplate.hasKey("jwt:blocklist:" + hashToken("test.jwt"))).thenReturn(
            Boolean.FALSE);

        boolean result = tokenBlocklistService.isBlocked("test.jwt");

        assertThat(result).isFalse();
    }

    @Test
    @DisplayName("Should return false when hasKey returns null")
    void isBlocked_null() {
        when(redisTemplate.hasKey("jwt:blocklist:" + hashToken("test.jwt"))).thenReturn(null);

        boolean result = tokenBlocklistService.isBlocked("test.jwt");

        assertThat(result).isFalse();
    }

    private String hashToken(final String token) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashBytes = digest.digest(token.getBytes(StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder(2 * hashBytes.length);
            for (byte b : hashBytes) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
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
