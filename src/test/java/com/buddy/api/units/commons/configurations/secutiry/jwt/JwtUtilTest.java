package com.buddy.api.units.commons.configurations.secutiry.jwt;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.Mockito.when;

import com.buddy.api.commons.configurations.properties.AuthProperties;
import com.buddy.api.commons.configurations.security.jwt.JwtUtil;
import com.buddy.api.units.UnitTestAbstract;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

class JwtUtilTest extends UnitTestAbstract {

    @Mock
    private AuthProperties properties;

    @Mock
    private HttpServletRequest request;

    @InjectMocks
    private JwtUtil jwtUtil;

    @Test
    @DisplayName("Should generate access token with email and profiles")
    void generateAccessToken_generatesValidToken() {
        when(properties.secretKey()).thenReturn(SECRET_KEY);
        when(properties.accessTokenExpiration()).thenReturn(
            Math.toIntExact(ACCESS_TOKEN_EXPIRATION)
        );

        String token = jwtUtil.generateAccessToken(EMAIL_VALUE, PROFILES);

        String emailFromToken = jwtUtil.getEmailFromToken(token);
        assertThat(token).isNotEmpty();
        assertThat(emailFromToken).isEqualTo(EMAIL_VALUE);
    }

    @Test
    @DisplayName("Should generate refresh token with email")
    void generateRefreshToken_generatesValidToken() {
        when(properties.secretKey()).thenReturn(SECRET_KEY);
        when(properties.refreshTokenExpiration()).thenReturn(
            Math.toIntExact(REFRESH_TOKEN_EXPIRATION)
        );

        String token = jwtUtil.generateRefreshToken(EMAIL_VALUE);

        String emailFromToken = jwtUtil.getEmailFromToken(token);
        assertThat(token).isNotEmpty();
        assertThat(emailFromToken).isEqualTo(EMAIL_VALUE);
    }

    @Test
    @DisplayName("Should extract email from valid token")
    void getEmailFromToken_extractsEmailCorrectly() {
        when(properties.secretKey()).thenReturn(SECRET_KEY);
        when(properties.accessTokenExpiration()).thenReturn(
            Math.toIntExact(ACCESS_TOKEN_EXPIRATION));

        String token = jwtUtil.generateAccessToken(EMAIL_VALUE, PROFILES);

        String extractedEmail = jwtUtil.getEmailFromToken(token);

        assertThat(extractedEmail).isEqualTo(EMAIL_VALUE);
    }

    @Test
    @DisplayName("Should throw JwtException when token is invalid")
    void getEmailFromToken_withInvalidToken_throwsJwtException() {
        String invalidToken = "invalid-token";
        when(properties.secretKey()).thenReturn(SECRET_KEY);

        assertThatThrownBy(() -> jwtUtil.getEmailFromToken(invalidToken))
            .isInstanceOf(JwtException.class)
            .hasMessage(
                "Invalid compact JWT string: "
                    + "Compact JWSs must contain exactly 2 period characters, "
                    + "and compact JWEs must contain exactly 4.  Found: 0"
            );
    }

    @Test
    @DisplayName("Should validate token when it matches username and is not expired")
    void validateToken_withValidToken_returnsTrue() {
        when(properties.secretKey()).thenReturn(SECRET_KEY);
        when(properties.accessTokenExpiration()).thenReturn(
            Math.toIntExact(ACCESS_TOKEN_EXPIRATION));
        String token = jwtUtil.generateAccessToken(EMAIL_VALUE, PROFILES);

        boolean isValid = jwtUtil.validateToken(token, EMAIL_VALUE);

        assertThat(isValid).isTrue();
    }

    @Test
    @DisplayName("Should return false when token does not match username")
    void validateToken_withWrongUsername_returnsFalse() {
        when(properties.secretKey()).thenReturn(SECRET_KEY);
        when(properties.accessTokenExpiration()).thenReturn(
            Math.toIntExact(ACCESS_TOKEN_EXPIRATION));
        String token = jwtUtil.generateAccessToken(EMAIL_VALUE, PROFILES);

        boolean isValid = jwtUtil.validateToken(token, "wrong@example.com");

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
    @DisplayName("Should extract JWT from cookies with specified name")
    void extractJwtFromCookies_extractsCorrectly() {
        String jwt = "jwt-token-sample";
        Cookie[] cookies = {new Cookie(ACCESS_TOKEN_COOKIE_NAME, jwt)};
        when(request.getCookies()).thenReturn(cookies);

        Optional<String> token = jwtUtil.extractJwtFromCookies(request, ACCESS_TOKEN_COOKIE_NAME);

        assertThat(token).isPresent().contains(jwt);
    }

    @Test
    @DisplayName("Should return empty when no matching cookie is found")
    void extractJwtFromCookies_withNoMatchingCookie_returnsEmpty() {
        Cookie[] cookies = {new Cookie("other_cookie", "other-value")};
        when(request.getCookies()).thenReturn(cookies);

        Optional<String> token = jwtUtil.extractJwtFromCookies(request, ACCESS_TOKEN_COOKIE_NAME);

        assertThat(token).isEmpty();
    }
}
