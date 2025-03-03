package com.buddy.api.domains.authentication.services.impls;

import static com.buddy.api.domains.profile.enums.ProfileTypeEnum.ADMIN;

import com.buddy.api.commons.configuration.security.jwt.JwtUtil;
import com.buddy.api.domains.account.services.UpdateAccount;
import com.buddy.api.domains.authentication.dtos.AuthDto;
import com.buddy.api.domains.authentication.services.AuthService;
import com.buddy.api.domains.profile.dtos.ProfileDto;
import com.buddy.api.domains.profile.services.FindProfile;
import jakarta.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final AuthenticationManager authenticationManager;
    private final UserDetailsService userDetailsService;
    private final FindProfile findProfile;
    private final JwtUtil jwtUtil;
    private final UpdateAccount updateAccount;

    public AuthDto authenticate(final AuthDto authDto) {
        authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(authDto.email(), authDto.password())
        );

        UserDetails userDetails = userDetailsService.loadUserByUsername(authDto.email());

        List<String> profileAuthorities = userDetails.getAuthorities().stream()
            .map(Object::toString)
            .toList();

        List<ProfileDto> profiles = findProfile.findByAccountEmail(userDetails.getUsername());

        List<ProfileDto> filteredProfiles = profiles.stream()
            .filter(profile -> !profile.isDeleted())
            .filter(profile -> !profile.profileType().equals(ADMIN))
            .toList();

        String accessToken = jwtUtil.generateAccessToken(authDto.email(), profileAuthorities);
        String refreshToken = jwtUtil.generateRefreshToken(authDto.email());

        updateAccount.updateLastLogin(userDetails.getUsername(), LocalDateTime.now());
        log.info("Authentication successful for user: {}", authDto.email());

        return new AuthDto(
            authDto.email(),
            authDto.password(),
            filteredProfiles,
            accessToken,
            refreshToken
        );
    }

    @Override
    public AuthDto refreshToken(
        final HttpServletRequest request
    ) {
        Optional<String> refreshToken = jwtUtil.extractRefreshToken(request);

        if (refreshToken.isEmpty()) {
            log.warn("No valid refresh token found in request or AuthDto");
            throw new IllegalArgumentException("Refresh token is required");
        }

        String token = refreshToken.get();
        String email = jwtUtil.getEmailFromToken(token);
        UserDetails userDetails = userDetailsService.loadUserByUsername(email);

        if (!jwtUtil.validateToken(token, email)) {
            log.warn("Invalid refresh token for email: {}", email);
            throw new IllegalArgumentException("Invalid refresh token");
        }

        log.debug("Generating new access token for email: {}", email);
        String newAccessToken = jwtUtil.generateAccessToken(email, userDetails.getAuthorities()
            .stream()
            .map(Object::toString)
            .toList());

        return new AuthDto(email, null, null, newAccessToken, token);
    }
}
