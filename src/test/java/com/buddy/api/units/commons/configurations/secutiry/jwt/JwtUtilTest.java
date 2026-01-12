package com.buddy.api.units.commons.configurations.secutiry.jwt;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import com.buddy.api.commons.configurations.properties.AuthProperties;
import com.buddy.api.commons.configurations.security.jwt.JwtUtil;
import com.buddy.api.units.UnitTestAbstract;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

class JwtUtilTest extends UnitTestAbstract {

    private static final String ACCESS_TOKEN_COOKIE_NAME = "access_token";
    private static final String REFRESH_TOKEN_COOKIE_NAME = "refresh_token";
    private static final String COOKIE_TOKEN = "cookie-token";

    @Mock
    private HttpServletRequest request;

    @InjectMocks
    private JwtUtil jwtUtil;

    @BeforeEach
    void setUp() {
        AuthProperties props = AuthProperties.builder()
            .secretKey(SECRET_KEY)
            .accessTokenExpiration(Math.toIntExact(ACCESS_TOKEN_EXPIRATION))
            .refreshTokenExpiration(Math.toIntExact(REFRESH_TOKEN_EXPIRATION))
            .allowedOrigins(List.of())
            .build();
        jwtUtil = new JwtUtil(props);
    }

    @Test
    @DisplayName("Should generate access token containing correct subject and profiles")
    void generateAccessToken_validClaims() {
        String token = jwtUtil.generateAccessToken(EMAIL_VALUE, PROFILES);
        Claims claims = parseClaims(token);

        assertThat(claims.getSubject()).isEqualTo(EMAIL_VALUE);
        assertThat(claims.get("profiles", List.class)).containsExactlyElementsOf(PROFILES);
        assertThat(claims.getExpiration()).isAfter(new Date());
    }

    @Test
    @DisplayName("Should generate refresh token containing correct subject")
    void generateRefreshToken_validSubject() {
        String token = jwtUtil.generateRefreshToken(EMAIL_VALUE);
        Claims claims = parseClaims(token);

        assertThat(claims.getSubject()).isEqualTo(EMAIL_VALUE);
        assertThat(claims.getExpiration()).isAfter(new Date());
    }

    @Test
    @DisplayName("Should extract email from a valid token")
    void getEmailFromToken_validToken() {
        String token = jwtUtil.generateAccessToken(EMAIL_VALUE, PROFILES);
        String extracted = jwtUtil.getEmailFromToken(token);

        assertThat(extracted).isEqualTo(EMAIL_VALUE);
    }

    @Test
    @DisplayName("Should throw JwtException when parsing invalid token")
    void getEmailFromToken_invalidToken() {
        assertThatThrownBy(() -> jwtUtil.getEmailFromToken("invalid-token"))
            .isInstanceOf(JwtException.class);
    }

    @Test
    @DisplayName("Should validate token when it matches username and is not expired")
    void validateToken_valid() {
        String token = jwtUtil.generateAccessToken(EMAIL_VALUE, PROFILES);
        assertThat(jwtUtil.validateToken(token, EMAIL_VALUE)).isTrue();
    }

    @Test
    @DisplayName("Should return false when token does not match username")
    void validateToken_wrongSubject() {
        String token = jwtUtil.generateAccessToken(EMAIL_VALUE, PROFILES);
        assertThat(jwtUtil.validateToken(token, "wrong@example.com")).isFalse();
    }

    @Test
    @DisplayName("Should return false for expired token")
    void validateToken_expired() throws InterruptedException {
        AuthProperties shortProps = AuthProperties.builder()
            .secretKey(SECRET_KEY)
            .accessTokenExpiration(1)
            .refreshTokenExpiration(1)
            .allowedOrigins(List.of())
            .build();
        JwtUtil shortJwt = new JwtUtil(shortProps);
        String token = shortJwt.generateAccessToken(EMAIL_VALUE, PROFILES);

        Thread.sleep(10);

        assertThat(shortJwt.validateToken(token, EMAIL_VALUE)).isFalse();
    }

    @Test
    @DisplayName("Should return false and log warning for malformed token")
    void validateToken_malformed() {
        assertThat(jwtUtil.validateToken("garbage-token", EMAIL_VALUE)).isFalse();
    }

    @Test
    @DisplayName("Should extract access token from cookie if present")
    void extractAccessToken_fromCookie() {
        Cookie cookie = new Cookie(ACCESS_TOKEN_COOKIE_NAME, COOKIE_TOKEN);
        when(request.getCookies()).thenReturn(new Cookie[] {cookie});

        Optional<String> opt = jwtUtil.extractAccessToken(request);
        assertThat(opt).isPresent().contains(COOKIE_TOKEN);
    }

    @Test
    @DisplayName("Should prioritize cookie over header for access token")
    void extractAccessToken_priorityCookie() {
        Cookie cookie = new Cookie(ACCESS_TOKEN_COOKIE_NAME, COOKIE_TOKEN);
        when(request.getCookies()).thenReturn(new Cookie[] {cookie});

        Optional<String> opt = jwtUtil.extractAccessToken(request);
        assertThat(opt).isPresent().contains(COOKIE_TOKEN);
    }

    @Test
    @DisplayName("Should extract access token from header when cookie is absent")
    void extractAccessToken_fromHeader() {
        when(request.getCookies()).thenReturn(null);
        when(request.getHeader(AUTHORIZATION_HEADER)).thenReturn(BEARER_TOKEN);

        Optional<String> opt = jwtUtil.extractAccessToken(request);
        assertThat(opt).isPresent().contains(BEARER_TOKEN.substring(7));
    }

    @Test
    @DisplayName("Should skip header if it does not start with Bearer")
    void extractAccessToken_invalidHeaderPrefix() {
        when(request.getCookies()).thenReturn(null);
        when(request.getHeader(AUTHORIZATION_HEADER)).thenReturn("Basic 123456");

        Optional<String> opt = jwtUtil.extractAccessToken(request);
        assertThat(opt).isEmpty();
    }

    @Test
    @DisplayName("Should extract refresh token from cookie if present")
    void extractRefreshToken_fromCookie() {
        Cookie cookie = new Cookie(REFRESH_TOKEN_COOKIE_NAME, REFRESH_TOKEN);
        when(request.getCookies()).thenReturn(new Cookie[] {cookie});

        Optional<String> opt = jwtUtil.extractRefreshToken(request);
        assertThat(opt).isPresent().contains(REFRESH_TOKEN);
    }

    @Test
    @DisplayName("Should extract refresh token from header when cookie is absent")
    void extractRefreshToken_fromHeader() {
        when(request.getCookies()).thenReturn(null);
        when(request.getHeader(AUTHORIZATION_HEADER)).thenReturn(BEARER_TOKEN);

        Optional<String> opt = jwtUtil.extractRefreshToken(request);
        assertThat(opt).isPresent().contains(BEARER_TOKEN.substring(7));
    }

    @Test
    @DisplayName("Should return empty when no tokens are present")
    void extractToken_none() {
        when(request.getCookies()).thenReturn(null);
        when(request.getHeader(AUTHORIZATION_HEADER)).thenReturn(null);

        assertThat(jwtUtil.extractAccessToken(request)).isEmpty();
        assertThat(jwtUtil.extractRefreshToken(request)).isEmpty();
    }

    @Test
    @DisplayName("Should ignore cookies with blank values")
    void extractToken_blankCookie() {
        Cookie cookie = new Cookie(ACCESS_TOKEN_COOKIE_NAME, "");
        when(request.getCookies()).thenReturn(new Cookie[] {cookie});
        when(request.getHeader(AUTHORIZATION_HEADER)).thenReturn(null);

        Optional<String> opt = jwtUtil.extractAccessToken(request);
        assertThat(opt).isEmpty();
    }

    private Claims parseClaims(final String token) {
        return Jwts.parser()
            .verifyWith(Keys.hmacShaKeyFor(SECRET_KEY.getBytes(StandardCharsets.UTF_8)))
            .build()
            .parseSignedClaims(token)
            .getPayload();
    }
}
