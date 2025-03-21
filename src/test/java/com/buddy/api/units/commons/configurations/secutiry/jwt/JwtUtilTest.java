package com.buddy.api.units.commons.configurations.secutiry.jwt;

import static java.lang.Math.toIntExact;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.buddy.api.commons.configurations.properties.AuthProperties;
import com.buddy.api.commons.configurations.security.jwt.JwtUtil;
import com.buddy.api.units.UnitTestAbstract;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import java.time.Instant;
import java.util.Date;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;

class JwtUtilTest extends UnitTestAbstract {

    @Mock
    private AuthProperties properties;

    @Mock
    private JwtEncoder jwtEncoder;

    @Mock
    private HttpServletRequest request;

    @InjectMocks
    private JwtUtil jwtUtil;

    @Test
    @DisplayName("Should generate access token with email and profiles")
    void generateAccessToken_generatesValidToken() {
        String validAccessToken = configureTokenMocks(
            Map.of("profiles", PROFILES),
            0,
            ACCESS_TOKEN_EXPIRATION
        );

        when(properties.secretKey()).thenReturn(SECRET_KEY);
        when(properties.accessTokenExpiration()).thenReturn(toIntExact(ACCESS_TOKEN_EXPIRATION));

        String generatedToken = jwtUtil.generateAccessToken(EMAIL_VALUE, PROFILES);
        String emailFromToken = jwtUtil.getEmailFromToken(generatedToken);

        assertThat(generatedToken).isEqualTo(validAccessToken);
        assertThat(emailFromToken).isEqualTo(EMAIL_VALUE);
    }

    @Test
    @DisplayName("Should generate refresh token with email")
    void generateRefreshToken_generatesValidToken() {
        String validRefreshToken = configureTokenMocks(
            Map.of(),
            0,
            REFRESH_TOKEN_EXPIRATION
        );

        when(properties.secretKey()).thenReturn(SECRET_KEY);
        when(properties.refreshTokenExpiration()).thenReturn(toIntExact(REFRESH_TOKEN_EXPIRATION));

        String generatedToken = jwtUtil.generateRefreshToken(EMAIL_VALUE);
        String emailFromToken = jwtUtil.getEmailFromToken(generatedToken);

        assertThat(generatedToken).isEqualTo(validRefreshToken);
        assertThat(emailFromToken).isEqualTo(EMAIL_VALUE);
    }

    @Test
    @DisplayName("Should extract email from valid token")
    void getEmailFromToken_extractsEmailCorrectly() {
        configureTokenMocks(
            Map.of("profiles", PROFILES),
            0,
            ACCESS_TOKEN_EXPIRATION
        );

        when(properties.secretKey()).thenReturn(SECRET_KEY);
        when(properties.accessTokenExpiration()).thenReturn(toIntExact(ACCESS_TOKEN_EXPIRATION));

        String generatedToken = jwtUtil.generateAccessToken(EMAIL_VALUE, PROFILES);
        String extractedEmail = jwtUtil.getEmailFromToken(generatedToken);

        assertThat(extractedEmail).isEqualTo(EMAIL_VALUE);
    }

    @Test
    @DisplayName("Should throw JwtException when token is invalid")
    void getEmailFromToken_withInvalidToken_throwsJwtException() {
        when(properties.secretKey()).thenReturn(SECRET_KEY);
        String invalidToken = "invalid-token";

        assertThatThrownBy(() -> jwtUtil.getEmailFromToken(invalidToken))
            .isInstanceOf(JwtException.class);
    }

    @Test
    @DisplayName("Should validate token when it matches username and is not expired")
    void validateToken_withValidToken_returnsTrue() {
        configureTokenMocks(
            Map.of("profiles", PROFILES),
            0,
            ACCESS_TOKEN_EXPIRATION
        );

        when(properties.secretKey()).thenReturn(SECRET_KEY);
        when(properties.accessTokenExpiration()).thenReturn(toIntExact(ACCESS_TOKEN_EXPIRATION));

        String generatedToken = jwtUtil.generateAccessToken(EMAIL_VALUE, PROFILES);
        boolean isValid = jwtUtil.validateToken(generatedToken, EMAIL_VALUE);

        assertThat(isValid).isTrue();
    }

    @Test
    @DisplayName("Should return false when token does not match username")
    void validateToken_withWrongUsername_returnsFalse() {
        configureTokenMocks(
            Map.of("profiles", PROFILES),
            0,
            ACCESS_TOKEN_EXPIRATION
        );

        when(properties.secretKey()).thenReturn(SECRET_KEY);
        when(properties.accessTokenExpiration()).thenReturn(toIntExact(ACCESS_TOKEN_EXPIRATION));

        String generatedToken = jwtUtil.generateAccessToken(EMAIL_VALUE, PROFILES);
        boolean isValid = jwtUtil.validateToken(generatedToken, "wrong@example.com");

        assertThat(isValid).isFalse();
    }

    @Test
    @DisplayName("Should extract refresh token from cookie")
    void extractRefreshToken_fromCookie_extractsCorrectly() {
        String refreshToken = "refresh-token-sample";
        Cookie[] cookies = {new Cookie(REFRESH_TOKEN_COOKIE_NAME, refreshToken)};
        when(request.getCookies()).thenReturn(cookies);

        Optional<String> token = jwtUtil.extractRefreshToken(request);

        assertThat(token).isPresent().contains(refreshToken);
    }

    @Test
    @DisplayName("Should extract refresh token from Authorization header when cookie is absent")
    void extractRefreshToken_fromHeader_extractsCorrectly() {
        String refreshToken = "refresh-token-sample";
        when(request.getCookies()).thenReturn(null);
        when(request.getHeader(AUTHORIZATION_HEADER)).thenReturn(BEARER_PREFIX + refreshToken);

        Optional<String> token = jwtUtil.extractRefreshToken(request);

        assertThat(token).isPresent().contains(refreshToken);
    }

    @Test
    @DisplayName("Should return empty when no refresh token is present")
    void extractRefreshToken_withNoToken_returnsEmpty() {
        when(request.getCookies()).thenReturn(null);
        when(request.getHeader(AUTHORIZATION_HEADER)).thenReturn(null);

        Optional<String> token = jwtUtil.extractRefreshToken(request);

        assertThat(token).isEmpty();
    }

    @Test
    @DisplayName("Should throw ExpiredJwtException when token is expired")
    void validateToken_withExpiredToken_throwsExpiredJwtException() {
        final var issuedAtOffset = -3600000;
        final var expiresAtOffset = -1800000;

        configureTokenMocks(
            Map.of(),
            issuedAtOffset,
            expiresAtOffset
        );

        when(properties.secretKey()).thenReturn(SECRET_KEY);
        when(properties.accessTokenExpiration()).thenReturn(toIntExact(ACCESS_TOKEN_EXPIRATION));

        String generatedToken = jwtUtil.generateAccessToken(EMAIL_VALUE, PROFILES);
        boolean validateToken = jwtUtil.validateToken(generatedToken, EMAIL_VALUE);

        assertThat(validateToken).isFalse();
    }

    private String configureTokenMocks(Map<String, Object> additionalClaims,
                                       long issuedAtOffset,
                                       long expiresAtOffset
    ) {

        long now = System.currentTimeMillis();
        String token = Jwts.builder()
            .subject(EMAIL_VALUE)
            .issuedAt(new Date(now + issuedAtOffset))
            .expiration(new Date(now + expiresAtOffset))
            .signWith(Keys.hmacShaKeyFor(SECRET_KEY.getBytes()))
            .claims(additionalClaims)
            .compact();

        Instant issuedAt = Instant.now().plusMillis(issuedAtOffset);
        Instant expiresAt = Instant.now().plusMillis(expiresAtOffset);

        when(jwtEncoder.encode(any(JwtEncoderParameters.class)))
            .thenReturn(Jwt.withTokenValue(token)
                .header("alg", "HS256")
                .subject(EMAIL_VALUE)
                .issuedAt(issuedAt)
                .expiresAt(expiresAt)
                .claims(claims -> claims.putAll(additionalClaims))
                .build());

        return token;
    }
}
