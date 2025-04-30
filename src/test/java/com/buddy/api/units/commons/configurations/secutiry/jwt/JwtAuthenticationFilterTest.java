package com.buddy.api.units.commons.configurations.secutiry.jwt;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.buddy.api.commons.configurations.security.jwt.JwtAuthenticationFilter;
import com.buddy.api.commons.configurations.security.jwt.JwtUtil;
import com.buddy.api.units.UnitTestAbstract;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;

class JwtAuthenticationFilterTest extends UnitTestAbstract {

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

    @InjectMocks
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Test
    @DisplayName("Should authenticate user when valid JWT is present in Authorization header")
    void doFilterInternal_withValidJwtInHeader_authenticatesUser() throws Exception {
        UserDetails userDetails = mock(UserDetails.class);

        when(request.getHeader(AUTHORIZATION_HEADER)).thenReturn(BEARER_TOKEN);
        when(jwtUtil.getEmailFromToken(VALID_JWT)).thenReturn(EMAIL_VALUE);
        when(userDetailsService.loadUserByUsername(EMAIL_VALUE)).thenReturn(userDetails);
        when(jwtUtil.validateToken(VALID_JWT, EMAIL_VALUE)).thenReturn(true);
        when(userDetails.getUsername()).thenReturn(EMAIL_VALUE);

        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        verify(request).getHeader(AUTHORIZATION_HEADER);
        verify(jwtUtil).getEmailFromToken(VALID_JWT);
        verify(userDetailsService).loadUserByUsername(EMAIL_VALUE);
        verify(jwtUtil).validateToken(VALID_JWT, EMAIL_VALUE);
        verify(filterChain).doFilter(request, response);

        assertThat(SecurityContextHolder.getContext().getAuthentication())
            .isNotNull()
            .extracting("principal")
            .isEqualTo(userDetails);
    }

    @Test
    @DisplayName("Should authenticate user when valid JWT is present in cookie")
    void doFilterInternal_withValidJwtInCookie_authenticatesUser() throws Exception {
        UserDetails userDetails = mock(UserDetails.class);
        Cookie[] cookies = {new Cookie(ACCESS_TOKEN_NAME, VALID_JWT)};

        when(request.getHeader(AUTHORIZATION_HEADER)).thenReturn(null);
        when(request.getCookies()).thenReturn(cookies);
        when(jwtUtil.getEmailFromToken(VALID_JWT)).thenReturn(EMAIL_VALUE);
        when(userDetailsService.loadUserByUsername(EMAIL_VALUE)).thenReturn(userDetails);
        when(jwtUtil.validateToken(VALID_JWT, EMAIL_VALUE)).thenReturn(true);
        when(userDetails.getUsername()).thenReturn(EMAIL_VALUE);

        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        verify(request).getHeader(AUTHORIZATION_HEADER);
        verify(request).getCookies();
        verify(jwtUtil).getEmailFromToken(VALID_JWT);
        verify(userDetailsService).loadUserByUsername(EMAIL_VALUE);
        verify(jwtUtil).validateToken(VALID_JWT, EMAIL_VALUE);
        verify(filterChain).doFilter(request, response);

        assertThat(SecurityContextHolder.getContext().getAuthentication())
            .isNotNull()
            .extracting("principal")
            .isEqualTo(userDetails);
    }

    @Test
    @DisplayName("Should proceed without authentication when no JWT is present")
    void doFilterInternal_withNoJwt_proceedsWithoutAuthentication() throws Exception {
        when(request.getHeader(AUTHORIZATION_HEADER)).thenReturn(null);
        when(request.getCookies()).thenReturn(null);

        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        verify(request).getHeader(AUTHORIZATION_HEADER);
        verify(request).getCookies();
        verify(jwtUtil, never()).getEmailFromToken(any());
        verify(userDetailsService, never()).loadUserByUsername(any());
        verify(jwtUtil, never()).validateToken(any(), any());
        verify(filterChain).doFilter(request, response);

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
    }

    @Test
    @DisplayName("Should proceed without authentication when JWT is invalid")
    void doFilterInternal_withInvalidJwt_proceedsWithoutAuthentication() throws Exception {
        when(request.getHeader(AUTHORIZATION_HEADER)).thenReturn(BEARER_TOKEN);
        when(jwtUtil.getEmailFromToken(VALID_JWT)).thenReturn(EMAIL_VALUE);
        when(userDetailsService.loadUserByUsername(EMAIL_VALUE))
            .thenThrow(new RuntimeException("Invalid JWT"));

        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        verify(request).getHeader(AUTHORIZATION_HEADER);
        verify(jwtUtil).getEmailFromToken(VALID_JWT);
        verify(userDetailsService).loadUserByUsername(EMAIL_VALUE);
        verify(jwtUtil, never()).validateToken(any(), any());
        verify(filterChain).doFilter(request, response);

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
    }
}
