package com.buddy.api.commons.configuration.security.jwt;

import com.buddy.api.commons.configuration.properties.AuthProperties;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Optional;
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
        log.debug("Generating access token for email: {}", email);
        return Jwts.builder()
            .subject(email)
            .claim("profiles", profiles)
            .issuedAt(new Date())
            .expiration(new Date(System.currentTimeMillis() + properties.accessTokenExpiration()))
            .signWith(Keys.hmacShaKeyFor(properties.secretKey().getBytes(StandardCharsets.UTF_8)))
            .compact();
    }

    public String generateRefreshToken(final String email) {
        log.debug("Generating refresh token for email: {}", email);
        return Jwts.builder()
            .subject(email)
            .issuedAt(new Date())
            .expiration(new Date(System.currentTimeMillis() + properties.refreshTokenExpiration()))
            .signWith(Keys.hmacShaKeyFor(properties.secretKey().getBytes(StandardCharsets.UTF_8)))
            .compact();
    }

    public String getEmailFromToken(final String token) throws JwtException {
        Claims claims = parseClaims(token);
        return claims.getSubject();
    }

    public boolean validateToken(final String token, final String username) {
        Claims claims = parseClaims(token);
        return claims.getSubject().equals(username) && !claims.getExpiration().before(new Date());
    }

    public Optional<String> extractRefreshToken(final HttpServletRequest request) {
        log.debug("Extracting refresh token from request");
        return extractJwtFromCookies(request, REFRESH_TOKEN_COOKIE_NAME)
            .or(() -> Optional.ofNullable(request.getHeader(AUTHORIZATION_HEADER))
                .filter(header -> header.startsWith(BEARER_PREFIX))
                .map(header -> header.substring(BEARER_PREFIX.length())));
    }

    public Optional<String> extractJwtFromCookies(final HttpServletRequest request,
                                                  final String cookieName
    ) {
        log.debug("Extracting JWT from cookies with name: {}", cookieName);
        return Optional.ofNullable(request.getCookies())
            .flatMap(cookies -> Arrays.stream(cookies)
                .filter(cookie -> cookieName.equals(cookie.getName()))
                .map(Cookie::getValue)
                .findFirst());
    }

    private Claims parseClaims(final String token) throws JwtException {
        return Jwts.parser()
            .verifyWith(Keys.hmacShaKeyFor(properties.secretKey().getBytes(StandardCharsets.UTF_8)))
            .build()
            .parseSignedClaims(token)
            .getPayload();
    }
}