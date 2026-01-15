package com.buddy.api.commons.configurations.security.jwt;

import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
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
        @NonNull final FilterChain filterChain
    ) throws ServletException, IOException {

        jwtUtil.extractAccessToken(request)
            .ifPresentOrElse(
                token -> processTokenAuthentication(request, token),
                () -> log.trace("No JWT found in request")
            );

        filterChain.doFilter(request, response);
    }

    private void processTokenAuthentication(final HttpServletRequest request, final String token) {
        try {
            final String email = jwtUtil.getEmailFromToken(token);

            if (shouldSkipAuthentication(email)) {
                return;
            }

            final UserDetails userDetails = userDetailsService.loadUserByUsername(email);

            if (!jwtUtil.validateToken(token, userDetails.getUsername())) {
                log.warn("Invalid JWT token for user: {}", email);
                return;
            }

            authenticateUser(request, userDetails);

        } catch (JwtException | IllegalArgumentException e) {
            log.warn("JWT validation failed: {}", e.getMessage());
        } catch (UsernameNotFoundException e) {
            log.warn("User from token not found: {}", e.getMessage());
        }
    }

    private boolean shouldSkipAuthentication(final String email) {
        return email == null || SecurityContextHolder.getContext().getAuthentication() != null;
    }

    private void authenticateUser(final HttpServletRequest request,
                                  final UserDetails userDetails
    ) {
        log.debug("User authenticated: {}", userDetails.getUsername());

        var authentication = new UsernamePasswordAuthenticationToken(
            userDetails,
            null,
            userDetails.getAuthorities()
        );

        authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }
}
