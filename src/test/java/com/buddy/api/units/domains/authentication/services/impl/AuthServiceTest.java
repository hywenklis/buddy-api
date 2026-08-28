package com.buddy.api.units.domains.authentication.services.impl;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import com.buddy.api.builders.profile.ProfileBuilder;
import com.buddy.api.commons.configurations.security.jwt.JwtUtil;
import com.buddy.api.commons.configurations.security.jwt.TokenBlocklistService;
import com.buddy.api.commons.exceptions.AccountBlockedException;
import com.buddy.api.commons.exceptions.AccountNotVerifiedException;
import com.buddy.api.commons.exceptions.AuthenticationException;
import com.buddy.api.domains.account.services.UpdateAccount;
import com.buddy.api.domains.authentication.dtos.AuthDto;
import com.buddy.api.domains.authentication.services.impl.AuthServiceImpl;
import com.buddy.api.domains.profile.enums.ProfileTypeEnum;
import com.buddy.api.domains.profile.services.FindProfile;
import com.buddy.api.domains.valueobjects.EmailAddress;
import com.buddy.api.units.UnitTestAbstract;
import com.buddy.api.utils.RandomEmailUtils;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.http.HttpServletRequest;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;

class AuthServiceTest extends UnitTestAbstract {
    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private UserDetailsService userDetailsService;

    @Mock
    private FindProfile findProfile;

    @Mock
    private JwtUtil jwtUtil;

    @Mock
    private UpdateAccount updateAccount;

    @Mock
    private TokenBlocklistService blocklistService;

    @Mock
    private HttpServletRequest request;

    @Mock
    private Authentication authResult;

    @InjectMocks
    private AuthServiceImpl authService;

    @Test
    @DisplayName("Should authenticate user successfully and return AuthDto")
    void should_authenticate_user_successfully() {
        final var authDto = AuthDto.builder()
            .email(RandomEmailUtils.generateValidEmail())
            .password(RandomStringUtils.secure().nextAlphanumeric(10))
            .build();

        final var userDetails = new User(
            authDto.email(),
            authDto.password(),
            List.of(new SimpleGrantedAuthority(ProfileTypeEnum.USER.name())));

        final var activeProfile = ProfileBuilder.profileDto().isDeleted(false).build();
        final var adminProfile =
            ProfileBuilder.profileDto().profileType(ProfileTypeEnum.ADMIN).isDeleted(false).build();
        final var deletedProfile =
            ProfileBuilder.profileDto().profileType(ProfileTypeEnum.ADMIN).isDeleted(true).build();
        final var profiles = List.of(activeProfile, adminProfile, deletedProfile);

        when(authResult.getPrincipal()).thenReturn(userDetails);

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
            .thenReturn(authResult);

        when(findProfile.findByAccountEmail(new EmailAddress(authDto.email())))
            .thenReturn(profiles);

        when(jwtUtil.generateAccessToken(
            authDto.email(),
            List.of(ProfileTypeEnum.USER.name()))
        ).thenReturn(ACCESS_TOKEN);

        when(jwtUtil.generateRefreshToken(eq(authDto.email()), anyString()))
            .thenReturn(REFRESH_TOKEN);

        AuthDto result = authService.authenticate(authDto);

        assertThat(result).isNotNull();
        assertThat(result.email()).isEqualTo(authDto.email());
        assertThat(result.password()).isNull();
        assertThat(result.profiles().size() == 1).isSameAs(true);
        assertThat(result.profiles()).isEqualTo(List.of(activeProfile));
        assertThat(result.accessToken()).isEqualTo(ACCESS_TOKEN);
        assertThat(result.refreshToken()).isEqualTo(REFRESH_TOKEN);

        verify(updateAccount, times(1))
            .updateLastLogin(eq(authDto.email()), any(LocalDateTime.class));

        verify(findProfile, times(1))
            .findByAccountEmail(new EmailAddress(authDto.email()));

        verify(jwtUtil, times(1))
            .generateAccessToken(authDto.email(), List.of(ProfileTypeEnum.USER.name()));

        verify(jwtUtil, times(1))
            .generateRefreshToken(eq(authDto.email()), anyString());
    }

    @Test
    @DisplayName("Should refresh token successfully and return new AuthDto")
    void should_refresh_token_successfully() {
        var email = RandomEmailUtils.generateValidEmail();
        UserDetails userDetails = new User(
            email,
            RandomStringUtils.secure().nextAlphanumeric(10),
            List.of(new SimpleGrantedAuthority(ProfileTypeEnum.USER.name()))
        );

        when(jwtUtil.extractRefreshToken(request)).thenReturn(Optional.of(REFRESH_TOKEN));
        when(blocklistService.isBlocked(REFRESH_TOKEN)).thenReturn(false);
        when(jwtUtil.getEmailFromToken(REFRESH_TOKEN)).thenReturn(email);
        when(jwtUtil.getIssuedAtFromToken(REFRESH_TOKEN)).thenReturn(Instant.ofEpochSecond(1000));
        when(blocklistService.isUserTokensRevoked(email, 1000000L)).thenReturn(false);
        when(userDetailsService.loadUserByUsername(email)).thenReturn(userDetails);
        when(jwtUtil.getExpirationInstantAllowingExpired(REFRESH_TOKEN))
            .thenReturn(Optional.of(Instant.now().plusSeconds(3600)));
        when(jwtUtil.validateToken(REFRESH_TOKEN, email)).thenReturn(true);
        when(jwtUtil.generateAccessToken(email, List.of(ProfileTypeEnum.USER.name())))
            .thenReturn(ACCESS_TOKEN);
        when(jwtUtil.generateRefreshToken(eq(email), anyString()))
            .thenReturn("new-refresh-token");

        AuthDto result = authService.refreshToken(request);

        assertThat(result).isNotNull();
        assertThat(result.email()).isEqualTo(email);
        assertThat(result.password()).isNull();
        assertThat(result.profiles()).isNull();
        assertThat(result.accessToken()).isEqualTo(ACCESS_TOKEN);
        assertThat(result.refreshToken()).isEqualTo("new-refresh-token");
        
        verify(blocklistService, times(1)).blockToken(eq(REFRESH_TOKEN), anyLong());

        verify(jwtUtil, times(1)).extractRefreshToken(request);
        verify(jwtUtil, times(1)).getEmailFromToken(REFRESH_TOKEN);
        verify(jwtUtil, times(1)).getIssuedAtFromToken(REFRESH_TOKEN);
        verify(blocklistService, times(1)).isUserTokensRevoked(email, 1000000L);
        verify(userDetailsService, times(1)).loadUserByUsername(email);
        verify(jwtUtil, times(1)).validateToken(REFRESH_TOKEN, email);
        verify(jwtUtil, times(1))
            .generateAccessToken(email, List.of(ProfileTypeEnum.USER.name()));
        verify(updateAccount, times(0))
            .updateLastLogin(any(String.class), any(LocalDateTime.class));
    }

    @Test
    @DisplayName("Should throw AuthenticationException when blockToken "
        + "fails with JwtException during refreshToken")
    void should_throw_auth_exception_when_block_token_fails_with_jwt_exception() {
        var email = RandomEmailUtils.generateValidEmail();
        UserDetails userDetails = new User(
            email,
            RandomStringUtils.secure().nextAlphanumeric(10),
            List.of(new SimpleGrantedAuthority(ProfileTypeEnum.USER.name()))
        );

        when(jwtUtil.extractRefreshToken(request)).thenReturn(Optional.of(REFRESH_TOKEN));
        when(blocklistService.isBlocked(REFRESH_TOKEN)).thenReturn(false);
        when(jwtUtil.getEmailFromToken(REFRESH_TOKEN)).thenReturn(email);
        when(jwtUtil.getIssuedAtFromToken(REFRESH_TOKEN)).thenReturn(Instant.ofEpochSecond(1000));
        when(blocklistService.isUserTokensRevoked(email, 1000000L)).thenReturn(false);
        when(userDetailsService.loadUserByUsername(email)).thenReturn(userDetails);
        when(jwtUtil.getExpirationInstantAllowingExpired(REFRESH_TOKEN))
            .thenThrow(new JwtException("Failed to parse token"));
        when(jwtUtil.validateToken(REFRESH_TOKEN, email)).thenReturn(true);
        when(jwtUtil.generateAccessToken(email, List.of(ProfileTypeEnum.USER.name())))
            .thenReturn(ACCESS_TOKEN);
        when(jwtUtil.generateRefreshToken(eq(email), anyString()))
            .thenReturn("new-refresh-token");

        assertThatThrownBy(() -> authService.refreshToken(request))
            .isInstanceOf(AuthenticationException.class)
            .hasMessage("Invalid refresh token or token expired")
            .hasFieldOrPropertyWithValue("fieldName", "refresh-token");
    }

    @Test
    @DisplayName("Should throw AuthenticationException when global token revocation is triggered")
    void should_throw_exception_when_global_token_revoked() {
        var email = RandomEmailUtils.generateValidEmail();
        UserDetails userDetails = new User(
            email,
            RandomStringUtils.secure().nextAlphanumeric(10),
            List.of(new SimpleGrantedAuthority(ProfileTypeEnum.USER.name()))
        );

        when(jwtUtil.extractRefreshToken(request)).thenReturn(Optional.of(REFRESH_TOKEN));
        when(blocklistService.isBlocked(REFRESH_TOKEN)).thenReturn(false);
        when(jwtUtil.getEmailFromToken(REFRESH_TOKEN)).thenReturn(email);
        when(userDetailsService.loadUserByUsername(email)).thenReturn(userDetails);
        when(jwtUtil.getIssuedAtFromToken(REFRESH_TOKEN)).thenReturn(Instant.ofEpochSecond(1000));
        when(blocklistService.isUserTokensRevoked(email, 1000000L)).thenReturn(true);

        assertThatThrownBy(() -> authService.refreshToken(request))
            .isInstanceOf(AuthenticationException.class)
            .hasMessage("Invalid refresh token or token expired")
            .hasFieldOrPropertyWithValue("fieldName", "refresh-token");

        verify(jwtUtil, times(1)).extractRefreshToken(request);
        verify(jwtUtil, times(1)).getEmailFromToken(REFRESH_TOKEN);
        verify(jwtUtil, times(1)).getIssuedAtFromToken(REFRESH_TOKEN);
        verify(blocklistService, times(1)).isUserTokensRevoked(email, 1000000L);
        verify(jwtUtil, never()).validateToken(any(), any());
        verify(jwtUtil, never()).generateAccessToken(any(), any());
    }

    @Test
    @DisplayName("Should throw IllegalArgumentException when refresh token is missing")
    void should_throw_exception_when_refresh_token_missing() {
        when(jwtUtil.extractRefreshToken(request)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.refreshToken(request))
            .isInstanceOf(AuthenticationException.class)
            .hasMessage("Refresh token is required");

        verify(jwtUtil, times(1)).extractRefreshToken(request);
        verify(jwtUtil, never()).getEmailFromToken(any());
        verify(userDetailsService, never()).loadUserByUsername(any());
        verify(jwtUtil, never()).validateToken(any(), any());
        verify(jwtUtil, never()).generateAccessToken(any(), any());
        verify(updateAccount, never()).updateLastLogin(any(), any());
    }

    @Test
    @DisplayName("Should throw IllegalArgumentException when refresh token is invalid")
    void should_throw_exception_when_refresh_token_invalid() {
        var email = RandomEmailUtils.generateValidEmail();
        UserDetails userDetails = new User(
            email,
            RandomStringUtils.secure().nextAlphanumeric(10),
            List.of(new SimpleGrantedAuthority(ProfileTypeEnum.USER.name()))
        );

        when(jwtUtil.extractRefreshToken(request)).thenReturn(Optional.of(REFRESH_TOKEN));
        when(blocklistService.isBlocked(REFRESH_TOKEN)).thenReturn(false);
        when(jwtUtil.getEmailFromToken(REFRESH_TOKEN)).thenReturn(email);
        when(jwtUtil.getIssuedAtFromToken(REFRESH_TOKEN)).thenReturn(Instant.ofEpochSecond(1000));
        when(blocklistService.isUserTokensRevoked(email, 1000000L)).thenReturn(false);
        when(userDetailsService.loadUserByUsername(email)).thenReturn(userDetails);
        when(jwtUtil.validateToken(REFRESH_TOKEN, email)).thenReturn(false);

        assertThatThrownBy(() -> authService.refreshToken(request))
            .isInstanceOf(AuthenticationException.class)
            .hasMessage("Invalid refresh token");

        verify(jwtUtil, times(1)).extractRefreshToken(request);
        verify(jwtUtil, times(1)).getEmailFromToken(REFRESH_TOKEN);
        verify(jwtUtil, times(1)).getIssuedAtFromToken(REFRESH_TOKEN);
        verify(blocklistService, times(1)).isUserTokensRevoked(email, 1000000L);
        verify(userDetailsService, times(1)).loadUserByUsername(email);
        verify(jwtUtil, times(1)).validateToken(REFRESH_TOKEN, email);
        verify(jwtUtil, never()).generateAccessToken(any(), any());
        verify(updateAccount, never()).updateLastLogin(any(), any());
    }

    @Test
    @DisplayName("Should throw AccountNotVerifiedException"
        + " when AuthenticationManager throws DisabledException")
    void should_throw_not_verified_when_disabled() {
        final var authDto = AuthDto.builder()
            .email(RandomEmailUtils.generateValidEmail())
            .password(UUID.randomUUID().toString())
            .build();

        when(authenticationManager.authenticate(any()))
            .thenThrow(new DisabledException("User is disabled"));

        assertThatThrownBy(() -> authService.authenticate(authDto))
            .isInstanceOf(AccountNotVerifiedException.class)
            .hasMessage(
                "account no longer active"
            );
    }

    @Test
    @DisplayName("Should throw AccountBlockedException "
        + "when AuthenticationManager throws LockedException")
    void should_throw_blocked_when_locked() {
        final var authDto = AuthDto.builder()
            .email(RandomEmailUtils.generateValidEmail())
            .password(UUID.randomUUID().toString())
            .build();

        when(authenticationManager.authenticate(any()))
            .thenThrow(new LockedException("User account is locked"));

        assertThatThrownBy(() -> authService.authenticate(authDto))
            .isInstanceOf(AccountBlockedException.class)
            .hasMessage("account blocked contact support");
    }

    @Test
    @DisplayName("Should throw AuthenticationException for bad credentials")
    void should_throw_authentication_exception_bad_credentials() {
        final var authDto = AuthDto.builder()
            .email(RandomEmailUtils.generateValidEmail())
            .password(UUID.randomUUID().toString())
            .build();

        when(authenticationManager.authenticate(any()))
            .thenThrow(new BadCredentialsException("Bad credentials"));

        assertThatThrownBy(() -> authService.authenticate(authDto))
            .isInstanceOf(AuthenticationException.class)
            .hasMessage("incorrect email or password");
    }

    @Test
    @DisplayName("Should throw AuthenticationException "
        + "when JwtException occurs during refresh (e.g. expired token)")
    void should_throw_auth_exception_when_jwt_exception_occurs() {
        when(jwtUtil.extractRefreshToken(request)).thenReturn(Optional.of(REFRESH_TOKEN));
        when(blocklistService.isBlocked(REFRESH_TOKEN)).thenReturn(false);

        when(jwtUtil.getEmailFromToken(REFRESH_TOKEN))
            .thenThrow(new io.jsonwebtoken.JwtException("Token expired or invalid"));

        assertThatThrownBy(() -> authService.refreshToken(request))
            .isInstanceOf(AuthenticationException.class)
            .hasMessage("Invalid refresh token or token expired")
            .hasFieldOrPropertyWithValue("fieldName", "refresh-token");

        verify(jwtUtil, times(1)).extractRefreshToken(request);
        verify(blocklistService, times(1)).isBlocked(REFRESH_TOKEN);
        verify(jwtUtil, times(1)).getEmailFromToken(REFRESH_TOKEN);

        verifyNoMoreInteractions(jwtUtil);
        verifyNoInteractions(userDetailsService);
        verifyNoInteractions(updateAccount);
        verifyNoInteractions(findProfile);
    }

    @Test
    @DisplayName("Should throw AuthenticationException when refresh token is blocked")
    void should_throw_auth_exception_when_refresh_token_blocked() {
        when(jwtUtil.extractRefreshToken(request)).thenReturn(Optional.of(REFRESH_TOKEN));
        when(blocklistService.isBlocked(REFRESH_TOKEN)).thenReturn(true);

        assertThatThrownBy(() -> authService.refreshToken(request))
            .isInstanceOf(AuthenticationException.class)
            .hasMessage("Invalid refresh token or token expired")
            .hasFieldOrPropertyWithValue("fieldName", "refresh-token");

        verify(jwtUtil, times(1)).extractRefreshToken(request);
        verify(blocklistService, times(1)).isBlocked(REFRESH_TOKEN);
        verifyNoInteractions(userDetailsService, findProfile, updateAccount);
    }

    @Test
    @DisplayName("Should block token successfully when expiration is in the future")
    void should_logout_successfully_and_block_token() {
        when(jwtUtil.getExpirationInstantFromToken(ACCESS_TOKEN))
            .thenReturn(Instant.now().plusSeconds(3600));

        authService.logout(ACCESS_TOKEN);

        ArgumentCaptor<Long> timeCaptor = ArgumentCaptor.forClass(Long.class);
        verify(blocklistService, times(1)).blockToken(eq(ACCESS_TOKEN), timeCaptor.capture());
        assertThat(timeCaptor.getValue()).isGreaterThan(0L);
    }

    @Test
    @DisplayName("Should not block token if it is already expired")
    void should_not_block_token_if_already_expired() {
        when(jwtUtil.getExpirationInstantFromToken(ACCESS_TOKEN))
            .thenReturn(Instant.parse("2020-01-01T00:00:00Z"));

        authService.logout(ACCESS_TOKEN);

        verifyNoInteractions(blocklistService);
    }

    @Test
    @DisplayName("Should handle JwtException gracefully on logout")
    void should_handle_jwt_exception_on_logout() {
        when(jwtUtil.getExpirationInstantFromToken(ACCESS_TOKEN))
            .thenThrow(new JwtException("Invalid JWT"));

        authService.logout(ACCESS_TOKEN);

        verifyNoInteractions(blocklistService);
    }

    @Test
    @DisplayName("Should execute logoutComplete successfully and block both tokens")
    void logoutComplete_success() {
        when(jwtUtil.extractAccessToken(request)).thenReturn(Optional.of(ACCESS_TOKEN));
        when(jwtUtil.extractRefreshToken(request)).thenReturn(Optional.of(REFRESH_TOKEN));
        when(jwtUtil.getEmailFromTokenAllowingExpired(ACCESS_TOKEN)).thenReturn(EMAIL_VALUE);

        when(jwtUtil.getExpirationInstantAllowingExpired(anyString()))
            .thenReturn(Optional.of(Instant.now().plusSeconds(3600)));

        authService.logoutComplete(request);

        verify(blocklistService, times(2)).blockToken(anyString(), anyLong());
    }

    @Test
    @DisplayName("Should execute logoutComplete successfully even when access token is expired")
    void logoutComplete_withExpiredAccessToken() {
        when(jwtUtil.extractAccessToken(request)).thenReturn(Optional.of(ACCESS_TOKEN));
        when(jwtUtil.extractRefreshToken(request)).thenReturn(Optional.of(REFRESH_TOKEN));
        when(jwtUtil.getEmailFromTokenAllowingExpired(ACCESS_TOKEN)).thenReturn(EMAIL_VALUE);

        when(jwtUtil.getExpirationInstantAllowingExpired(ACCESS_TOKEN))
            .thenReturn(Optional.of(Instant.now().minusSeconds(3600)));
        when(jwtUtil.getExpirationInstantAllowingExpired(REFRESH_TOKEN))
            .thenReturn(Optional.of(Instant.now().plusSeconds(3600)));

        authService.logoutComplete(request);

        verify(blocklistService, times(1)).blockToken(eq(REFRESH_TOKEN), anyLong());
    }

    @Test
    @DisplayName("Should throw AuthenticationException when access token "
        + "is missing in logoutComplete")
    void logoutComplete_missingAccessToken() {
        when(jwtUtil.extractAccessToken(request)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.logoutComplete(request))
            .isInstanceOf(AuthenticationException.class)
            .hasMessage("Access token is required")
            .hasFieldOrPropertyWithValue("fieldName", "access-token");
    }

    @Test
    @DisplayName("Should throw AuthenticationException when refresh token "
        + "is missing in logoutComplete")
    void logoutComplete_missingRefreshToken() {
        when(jwtUtil.extractAccessToken(request)).thenReturn(Optional.of(ACCESS_TOKEN));
        when(jwtUtil.extractRefreshToken(request)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.logoutComplete(request))
            .isInstanceOf(AuthenticationException.class)
            .hasMessage("Refresh token is required")
            .hasFieldOrPropertyWithValue("fieldName", "refresh-token");
    }

    @Test
    @DisplayName("Should throw AuthenticationException when token format "
        + "is invalid in logoutComplete")
    void logoutComplete_invalidTokenFormat() {
        when(jwtUtil.extractAccessToken(request)).thenReturn(Optional.of("invalid.token"));
        when(jwtUtil.extractRefreshToken(request)).thenReturn(Optional.of(REFRESH_TOKEN));
        when(jwtUtil.getEmailFromTokenAllowingExpired("invalid.token"))
            .thenThrow(new JwtException("Malformed token"));

        assertThatThrownBy(() -> authService.logoutComplete(request))
            .isInstanceOf(AuthenticationException.class)
            .hasMessage("Invalid token format")
            .hasFieldOrPropertyWithValue("fieldName", "token");
    }

    @Test
    @DisplayName("Should throw AuthenticationException when expired access token "
        + "is accompanied by invalid refresh token")
    void logoutComplete_withExpiredAccessTokenAndInvalidRefreshToken() {
        when(jwtUtil.extractAccessToken(request)).thenReturn(Optional.of(ACCESS_TOKEN));
        when(jwtUtil.extractRefreshToken(request)).thenReturn(Optional.of("invalid.refresh.token"));
        when(jwtUtil.getEmailFromTokenAllowingExpired(ACCESS_TOKEN)).thenReturn(EMAIL_VALUE);
        when(jwtUtil.getEmailFromTokenAllowingExpired("invalid.refresh.token"))
            .thenThrow(new JwtException("Malformed refresh token"));

        assertThatThrownBy(() -> authService.logoutComplete(request))
            .isInstanceOf(AuthenticationException.class)
            .hasMessage("Invalid token format")
            .hasFieldOrPropertyWithValue("fieldName", "token");
    }
}
