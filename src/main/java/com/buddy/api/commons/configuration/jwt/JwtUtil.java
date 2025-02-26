package com.buddy.api.commons.configuration.jwt;

import com.buddy.api.commons.configuration.properties.JwtProperties;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class JwtUtil {
    private final JwtProperties properties;

    public String generateAccessToken(final UUID accountId, final List<String> profiles) {
        return Jwts.builder()
            .subject(accountId.toString())
            .claim("profiles", profiles)
            .issuedAt(new Date())
            .expiration(new Date(System.currentTimeMillis() + properties.accessTokenExpiration()))
            .signWith(Keys.hmacShaKeyFor(properties.secretKey().getBytes(StandardCharsets.UTF_8)))
            .compact();
    }

    public String generateRefreshToken(final UUID accountId) {
        return Jwts.builder()
            .subject(accountId.toString())
            .issuedAt(new Date())
            .expiration(new Date(System.currentTimeMillis() + properties.refreshTokenExpiration()))
            .signWith(Keys.hmacShaKeyFor(properties.secretKey().getBytes(StandardCharsets.UTF_8)))
            .compact();
    }

    public UUID getAccountIdFromToken(final String token) throws JwtException {
        Claims claims = parseClaims(token);
        return UUID.fromString(claims.getSubject());
    }

    public List<String> getProfilesFromToken(final String token) throws JwtException {
        Claims claims = parseClaims(token);
        Object profilesClaim = claims.get("profiles");

        if (profilesClaim instanceof List<?> profileList) {
            if (profileList.stream().allMatch(String.class::isInstance)) {
                return profileList.stream()
                    .map(String.class::cast)
                    .toList();
            }
            throw new JwtException("Profiles claim contains non-String values");
        }
        return Collections.emptyList();
    }

    private Claims parseClaims(final String token) throws JwtException {
        return Jwts.parser()
            .verifyWith(Keys.hmacShaKeyFor(properties.secretKey().getBytes(StandardCharsets.UTF_8)))
            .build()
            .parseSignedClaims(token)
            .getPayload();
    }
}
