package com.buddy.api.units.domains.services.impls;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.buddy.api.builders.profile.ProfileBuilder;
import com.buddy.api.commons.configurations.security.jwt.JwtUtil;
import com.buddy.api.domains.account.services.UpdateAccount;
import com.buddy.api.domains.authentication.dtos.AuthDto;
import com.buddy.api.domains.authentication.services.impls.AuthServiceImpl;
import com.buddy.api.domains.profile.enums.ProfileTypeEnum;
import com.buddy.api.domains.profile.services.FindProfile;
import com.buddy.api.units.UnitTestAbstract;
import com.buddy.api.utils.RandomEmailUtils;
import jakarta.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
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
    private HttpServletRequest request;

    @InjectMocks
    private AuthServiceImpl authService;

    @Test
    @DisplayName("Should authenticate user successfully and return AuthDto")
    void should_authenticate_user_successfully() {
        var authDto = AuthDto.builder()
            .email(RandomEmailUtils.generateValidEmail())
            .password(RandomStringUtils.secure().nextAlphanumeric(10))
            .build();

        var userDetails = new User(
            authDto.email(),
            authDto.password(),
            List.of(new SimpleGrantedAuthority(ProfileTypeEnum.USER.name())));

        var authentication = new UsernamePasswordAuthenticationToken(
            authDto.email(),
            authDto.password()
        );

        var activeProfile = ProfileBuilder.profileDto().isDeleted(false).build();

        var adminProfile = ProfileBuilder
            .profileDto()
            .profileType(ProfileTypeEnum.ADMIN)
            .isDeleted(false)
            .build();

        var deletedProfile = ProfileBuilder
            .profileDto()
            .profileType(ProfileTypeEnum.ADMIN)
            .isDeleted(true)
            .build();

        var profiles = List.of(activeProfile, adminProfile, deletedProfile);

        when(authenticationManager.authenticate(authentication)).thenReturn(null);
        when(userDetailsService.loadUserByUsername(authDto.email())).thenReturn(userDetails);
        when(findProfile.findByAccountEmail(authDto.email())).thenReturn(profiles);

        when(jwtUtil.generateAccessToken(
            authDto.email(),
            List.of(ProfileTypeEnum.USER.name()))
        ).thenReturn(ACCESS_TOKEN);

        when(jwtUtil.generateRefreshToken(authDto.email())).thenReturn(REFRESH_TOKEN);

        AuthDto result = authService.authenticate(authDto);

        assertThat(result).isNotNull();
        assertThat(result.email()).isEqualTo(authDto.email());
        assertThat(result.password()).isEqualTo(authDto.password());
        assertThat(result.profiles().size() == 1).isSameAs(true);
        assertThat(result.profiles()).isEqualTo(List.of(activeProfile));
        assertThat(result.accessToken()).isEqualTo(ACCESS_TOKEN);
        assertThat(result.refreshToken()).isEqualTo(REFRESH_TOKEN);

        verify(updateAccount, times(1))
            .updateLastLogin(eq(authDto.email()), any(LocalDateTime.class));

        verify(userDetailsService, times(1))
            .loadUserByUsername(authDto.email());

        verify(findProfile, times(1))
            .findByAccountEmail(authDto.email());

        verify(jwtUtil, times(1))
            .generateAccessToken(authDto.email(), List.of(ProfileTypeEnum.USER.name()));

        verify(jwtUtil, times(1))
            .generateRefreshToken(authDto.email());
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
        when(jwtUtil.getEmailFromToken(REFRESH_TOKEN)).thenReturn(email);
        when(userDetailsService.loadUserByUsername(email)).thenReturn(userDetails);
        when(jwtUtil.validateToken(REFRESH_TOKEN, email)).thenReturn(true);
        when(jwtUtil.generateAccessToken(email, List.of(ProfileTypeEnum.USER.name())))
            .thenReturn(ACCESS_TOKEN);

        AuthDto result = authService.refreshToken(request);

        assertThat(result).isNotNull();
        assertThat(result.email()).isEqualTo(email);
        assertThat(result.password()).isNull();
        assertThat(result.profiles()).isNull();
        assertThat(result.accessToken()).isEqualTo(ACCESS_TOKEN);
        assertThat(result.refreshToken()).isEqualTo(REFRESH_TOKEN);

        verify(jwtUtil, times(1)).extractRefreshToken(request);
        verify(jwtUtil, times(1)).getEmailFromToken(REFRESH_TOKEN);
        verify(userDetailsService, times(1)).loadUserByUsername(email);
        verify(jwtUtil, times(1)).validateToken(REFRESH_TOKEN, email);
        verify(jwtUtil, times(1))
            .generateAccessToken(email, List.of(ProfileTypeEnum.USER.name()));
        verify(updateAccount, times(0))
            .updateLastLogin(any(String.class), any(LocalDateTime.class));
    }

    @Test
    @DisplayName("Should throw IllegalArgumentException when refresh token is missing")
    void should_throw_exception_when_refresh_token_missing() {
        when(jwtUtil.extractRefreshToken(request)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.refreshToken(request))
            .isInstanceOf(IllegalArgumentException.class)
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
        when(jwtUtil.getEmailFromToken(REFRESH_TOKEN)).thenReturn(email);
        when(userDetailsService.loadUserByUsername(email)).thenReturn(userDetails);
        when(jwtUtil.validateToken(REFRESH_TOKEN, email)).thenReturn(false);

        assertThatThrownBy(() -> authService.refreshToken(request))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("Invalid refresh token");

        verify(jwtUtil, times(1)).extractRefreshToken(request);
        verify(jwtUtil, times(1)).getEmailFromToken(REFRESH_TOKEN);
        verify(userDetailsService, times(1)).loadUserByUsername(email);
        verify(jwtUtil, times(1)).validateToken(REFRESH_TOKEN, email);
        verify(jwtUtil, never()).generateAccessToken(any(), any());
        verify(updateAccount, never()).updateLastLogin(any(), any());
    }
}
