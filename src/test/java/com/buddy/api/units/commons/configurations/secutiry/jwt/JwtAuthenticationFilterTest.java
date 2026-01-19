package com.buddy.api.units.commons.configurations.secutiry.jwt;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.buddy.api.commons.configurations.security.jwt.JwtAuthenticationFilter;
import com.buddy.api.commons.configurations.security.jwt.JwtUtil;
import com.buddy.api.units.UnitTestAbstract;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.Optional;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

class JwtAuthenticationFilterTest extends UnitTestAbstract {

    private static final String VALID_JWT = "valid.jwt.token";
    private static final String EMAIL_VALUE = "user@example.com";

    @Mock
    private JwtUtil jwtUtil;

    @Mock
    private UserDetailsService userDetailsService;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private FilterChain filterChain;

    @Mock
    private UserDetails userDetails;

    @InjectMocks
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("Should authenticate user when JwtUtil extracts a valid token")
    void doFilter_withValidToken_authenticatesUser() throws Exception {
        when(jwtUtil.extractAccessToken(request)).thenReturn(Optional.of(VALID_JWT));
        when(jwtUtil.getEmailFromToken(VALID_JWT)).thenReturn(EMAIL_VALUE);
        when(userDetailsService.loadUserByUsername(EMAIL_VALUE)).thenReturn(userDetails);
        when(jwtUtil.validateToken(VALID_JWT, EMAIL_VALUE)).thenReturn(true);
        when(userDetails.getUsername()).thenReturn(EMAIL_VALUE);

        jwtAuthenticationFilter.doFilter(request, response, filterChain);

        verify(jwtUtil, times(1)).extractAccessToken(request);
        verify(jwtUtil, times(1)).getEmailFromToken(VALID_JWT);
        verify(userDetailsService, times(1)).loadUserByUsername(EMAIL_VALUE);
        verify(jwtUtil, times(1)).validateToken(VALID_JWT, EMAIL_VALUE);
        verify(filterChain, times(1)).doFilter(request, response);

        assertThat(SecurityContextHolder.getContext().getAuthentication())
            .isNotNull()
            .extracting("principal")
            .isEqualTo(userDetails);
    }

    @Test
    @DisplayName("Should proceed without authentication when JwtUtil finds no token")
    void doFilter_withNoToken_proceedsWithoutAuthentication() throws Exception {
        when(jwtUtil.extractAccessToken(request)).thenReturn(Optional.empty());

        jwtAuthenticationFilter.doFilter(request, response, filterChain);

        verify(jwtUtil, times(1)).extractAccessToken(request);
        verify(jwtUtil, never()).getEmailFromToken(any());
        verify(filterChain, times(1)).doFilter(request, response);
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
    }

    @Test
    @DisplayName("Should skip authentication when extracted email is null")
    void doFilter_withNullEmail_skipsAuthentication() throws Exception {
        when(jwtUtil.extractAccessToken(request)).thenReturn(Optional.of(VALID_JWT));
        when(jwtUtil.getEmailFromToken(VALID_JWT)).thenReturn(null);

        jwtAuthenticationFilter.doFilter(request, response, filterChain);

        verify(userDetailsService, never()).loadUserByUsername(any());
        verify(filterChain, times(1)).doFilter(request, response);
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
    }

    @Test
    @DisplayName("Should skip authentication when SecurityContext already has authentication")
    void doFilter_withExistingAuthentication_skipsAuthentication() throws Exception {
        when(jwtUtil.extractAccessToken(request)).thenReturn(Optional.of(VALID_JWT));
        when(jwtUtil.getEmailFromToken(VALID_JWT)).thenReturn(EMAIL_VALUE);

        SecurityContextHolder.getContext().setAuthentication(mock(Authentication.class));

        jwtAuthenticationFilter.doFilter(request, response, filterChain);

        verify(userDetailsService, never()).loadUserByUsername(any());
        verify(filterChain, times(1)).doFilter(request, response);
    }

    @Test
    @DisplayName("Should not authenticate when token validation returns false")
    void doFilter_withInvalidTokenValidation_doesNotAuthenticate() throws Exception {
        when(userDetails.getUsername()).thenReturn(EMAIL_VALUE);
        when(jwtUtil.extractAccessToken(request)).thenReturn(Optional.of(VALID_JWT));
        when(jwtUtil.getEmailFromToken(VALID_JWT)).thenReturn(EMAIL_VALUE);
        when(userDetailsService.loadUserByUsername(EMAIL_VALUE)).thenReturn(userDetails);
        when(jwtUtil.validateToken(VALID_JWT, EMAIL_VALUE)).thenReturn(false);

        jwtAuthenticationFilter.doFilter(request, response, filterChain);

        verify(jwtUtil, times(1)).validateToken(VALID_JWT, EMAIL_VALUE);
        verify(filterChain, times(1)).doFilter(request, response);

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
    }

    @Test
    @DisplayName("Should handle JwtException gracefully without crashing")
    void doFilter_withJwtException_proceedsWithoutAuthentication() throws Exception {
        when(jwtUtil.extractAccessToken(request)).thenReturn(Optional.of(VALID_JWT));
        when(jwtUtil.getEmailFromToken(VALID_JWT)).thenThrow(new JwtException("Expired token"));

        jwtAuthenticationFilter.doFilter(request, response, filterChain);

        verify(filterChain, times(1)).doFilter(request, response);
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
    }

    @Test
    @DisplayName("Should handle UsernameNotFoundException gracefully without crashing")
    void doFilter_withUserNotFound_proceedsWithoutAuthentication() throws Exception {
        when(jwtUtil.extractAccessToken(request)).thenReturn(Optional.of(VALID_JWT));
        when(jwtUtil.getEmailFromToken(VALID_JWT)).thenReturn(EMAIL_VALUE);
        when(userDetailsService.loadUserByUsername(EMAIL_VALUE))
            .thenThrow(new UsernameNotFoundException("User deleted"));

        jwtAuthenticationFilter.doFilter(request, response, filterChain);

        verify(filterChain, times(1)).doFilter(request, response);
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
    }

    @Test
    @DisplayName("Should throw NullPointerException (from Lombok @NonNull) when arguments are null")
    void doFilter_withNullParams_throwsException() {
        assertThatThrownBy(() -> jwtAuthenticationFilter.doFilter(
            null, response, filterChain
        )).isInstanceOf(Exception.class);

        assertThatThrownBy(() -> jwtAuthenticationFilter.doFilter(
            request, null, filterChain
        )).isInstanceOf(Exception.class);

        assertThatThrownBy(() -> jwtAuthenticationFilter.doFilter(
            request, response, null
        )).isInstanceOf(Exception.class);
    }
}