package com.buddy.api.commons.configuration.security.jwt;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Arrays;
import java.util.Optional;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
@RequiredArgsConstructor
@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final UserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(
        @NonNull final HttpServletRequest request,
        @NonNull final HttpServletResponse response,
        final FilterChain filterChain
    ) throws ServletException, IOException {
        extractJwtFromRequest(request)
            .flatMap(this::validateAndGetUserDetails)
            .ifPresentOrElse(
                userDetails -> authenticateUser(request, userDetails),
                () -> log.debug("No valid JWT found in request")
            );

        filterChain.doFilter(request, response);
    }

    private Optional<String> extractJwtFromRequest(final HttpServletRequest request) {
        return Optional.ofNullable(request.getHeader("Authorization"))
            .filter(header -> header.startsWith("Bearer "))
            .map(header -> header.substring(7))
            .or(() -> extractJwtFromCookies(request));
    }

    private Optional<String> extractJwtFromCookies(final HttpServletRequest request) {
        return Optional.ofNullable(request.getCookies())
            .flatMap(cookies -> Arrays.stream(cookies)
                .filter(cookie -> "access_token".equals(cookie.getName()))
                .map(Cookie::getValue)
                .findFirst());
    }

    private Optional<UserDetails> validateAndGetUserDetails(final String jwt) {
        try {
            String email = jwtUtil.getEmailFromToken(jwt);
            UserDetails userDetails = userDetailsService.loadUserByUsername(email);
            return jwtUtil.validateToken(jwt, userDetails.getUsername())
                ? Optional.of(userDetails)
                : Optional.empty();
        } catch (Exception e) {
            logger.error("Failed to validate JWT: {}", e);
            return Optional.empty();
        }
    }

    private void authenticateUser(final HttpServletRequest request, final UserDetails userDetails) {
        log.info("User authenticated: {}", userDetails.getUsername());
        UsernamePasswordAuthenticationToken authentication =
            new UsernamePasswordAuthenticationToken(
                userDetails,
                null,
                userDetails.getAuthorities()
            );

        authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }
}
