package com.buddy.api.commons.configurations.security.jwt;

import com.buddy.api.commons.configurations.properties.AuthProperties;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import javax.crypto.SecretKey;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtUtil {

    private final AuthProperties properties;

    private static final String ACCESS_TOKEN_COOKIE_NAME = "access_token";
    private static final String REFRESH_TOKEN_COOKIE_NAME = "refresh_token";
    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String BEARER_PREFIX = "Bearer ";

    public String generateAccessToken(final String email, final List<String> profiles) {
        Instant now = Instant.now();
        return Jwts.builder()
            .subject(email)
            .claim("profiles", profiles)
            .issuedAt(Date.from(now))
            .expiration(Date.from(now.plusMillis(properties.accessTokenExpiration())))
            .signWith(key())
            .compact();
    }

    public String generateRefreshToken(final String email) {
        Instant now = Instant.now();
        return Jwts.builder()
            .subject(email)
            .issuedAt(Date.from(now))
            .expiration(Date.from(now.plusMillis(properties.refreshTokenExpiration())))
            .signWith(key())
            .compact();
    }

    public Optional<String> extractAccessToken(final HttpServletRequest request) {
        return extractTokenFromCookies(request, ACCESS_TOKEN_COOKIE_NAME)
            .or(() -> extractTokenFromHeader(request));
    }

    public Optional<String> extractRefreshToken(final HttpServletRequest request) {
        return extractTokenFromCookies(request, REFRESH_TOKEN_COOKIE_NAME)
            .or(() -> extractTokenFromHeader(request));
    }

    public String getEmailFromToken(final String token) throws JwtException {
        return parseClaims(token).getSubject();
    }

    public boolean validateToken(final String token, final String username) {
        try {
            Claims claims = parseClaims(token);

            return claims.getSubject().equals(username);
        } catch (JwtException e) {
            log.warn("Token validation failed: {}", e.getMessage());
            return false;
        }
    }

    private Optional<String> extractTokenFromCookies(final HttpServletRequest request,
                                                     final String cookieName
    ) {
        if (request.getCookies() == null) {
            return Optional.empty();
        }

        return Arrays.stream(request.getCookies())
            .filter(cookie -> cookieName.equals(cookie.getName()))
            .map(Cookie::getValue)
            .filter(val -> !val.isBlank())
            .findFirst();
    }

    private Optional<String> extractTokenFromHeader(final HttpServletRequest request) {
        return Optional.ofNullable(request.getHeader(AUTHORIZATION_HEADER))
            .filter(header -> header.startsWith(BEARER_PREFIX))
            .map(header -> header.substring(BEARER_PREFIX.length()));
    }

    private Claims parseClaims(final String token) throws JwtException {
        Jws<Claims> jws = Jwts.parser()
            .verifyWith(key())
            .build()
            .parseSignedClaims(token);
        return jws.getPayload();
    }

    private SecretKey key() {
        byte[] keyBytes = properties.secretKey()
            .getBytes(StandardCharsets.UTF_8);
        return Keys.hmacShaKeyFor(keyBytes);
    }
}
