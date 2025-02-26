package com.buddy.api.commons.configuration.jwt;

import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;

    @Override
    protected void doFilterInternal(final HttpServletRequest request,
                                    final HttpServletResponse response,
                                    final FilterChain chain
    ) throws ServletException, IOException {
        extractToken(request)
            .flatMap(this::authenticateToken)
            .ifPresentOrElse(
                SecurityContextHolder.getContext()::setAuthentication,
                SecurityContextHolder::clearContext
            );
        chain.doFilter(request, response);
    }

    private Optional<String> extractToken(final HttpServletRequest request) {
        return Optional.ofNullable(request.getHeader("Authorization"))
            .filter(header -> header.startsWith("Bearer "))
            .map(header -> header.substring(7));
    }

    private Optional<UsernamePasswordAuthenticationToken> authenticateToken(final String token) {
        try {
            UUID accountId = jwtUtil.getAccountIdFromToken(token);
            List<SimpleGrantedAuthority> authorities = jwtUtil.getProfilesFromToken(token).stream()
                .map(SimpleGrantedAuthority::new)
                .toList();

            return Optional.of(new UsernamePasswordAuthenticationToken(
                accountId.toString(),
                null,
                authorities
            ));
        } catch (JwtException e) {
            return Optional.empty();
        }
    }
}
