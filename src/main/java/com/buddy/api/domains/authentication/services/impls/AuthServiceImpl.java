package com.buddy.api.domains.authentication.services.impls;

import static com.buddy.api.domains.profile.enums.ProfileTypeEnum.ADMIN;

import com.buddy.api.commons.configurations.security.jwt.JwtUtil;
import com.buddy.api.commons.exceptions.AuthenticationException;
import com.buddy.api.domains.account.services.UpdateAccount;
import com.buddy.api.domains.authentication.dtos.AuthDto;
import com.buddy.api.domains.authentication.services.AuthService;
import com.buddy.api.domains.profile.dtos.ProfileDto;
import com.buddy.api.domains.profile.services.FindProfile;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private static final Logger log = LoggerFactory.getLogger(AuthServiceImpl.class);

    private final AuthenticationManager authenticationManager;
    private final UserDetailsService userDetailsService;
    private final FindProfile findProfile;
    private final JwtUtil jwtUtil;
    private final UpdateAccount updateAccount;

    @Override
    @Transactional
    public AuthDto authenticate(final AuthDto authDto) {
        UserDetails userDetails = authenticateUser(authDto.email(), authDto.password());
        List<String> profileAuthorities = extractAuthorities(userDetails);
        List<ProfileDto> filteredProfiles = fetchAndFilterProfiles(userDetails.getUsername());

        String accessToken = jwtUtil.generateAccessToken(authDto.email(), profileAuthorities);
        String refreshToken = jwtUtil.generateRefreshToken(authDto.email());

        updateAccount.updateLastLogin(userDetails.getUsername(), LocalDateTime.now());
        log.info("Authentication successful for user: {}", authDto.email());

        return new AuthDto(
            authDto.email(),
            null,
            filteredProfiles,
            accessToken,
            refreshToken
        );
    }

    @Override
    @Transactional(readOnly = true)
    public AuthDto refreshToken(final HttpServletRequest request) {
        try {
            String refreshToken = extractRefreshToken(request)
                .orElseThrow(() -> {
                    log.warn("No valid refresh token found in request");
                    return new AuthenticationException("Refresh token is required",
                        "refresh-token");
                });


            String email = jwtUtil.getEmailFromToken(refreshToken);
            UserDetails userDetails = userDetailsService.loadUserByUsername(email);

            if (!jwtUtil.validateToken(refreshToken, email)) {
                log.warn("Invalid refresh token for email: {}", email);
                throw new AuthenticationException("Invalid refresh token", "refresh-token");
            }

            List<String> authorities = extractAuthorities(userDetails);
            String newAccessToken = jwtUtil.generateAccessToken(email, authorities);

            log.info("Refresh token successful for email: {}", email);
            return new AuthDto(email, null, null, newAccessToken, refreshToken);
        } catch (JwtException e) {
            log.error("Error refreshing token: {}", e.getMessage());
            throw new AuthenticationException(
                "Invalid refresh token or token expired",
                "refresh-token"
            );
        }
    }

    private UserDetails authenticateUser(final String email, final String password) {
        try {
            authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(email, password)
            );
            return userDetailsService.loadUserByUsername(email);
        } catch (Exception ex) {
            log.error("Authentication failed for user: {}", email, ex);
            throw new AuthenticationException("Authentication error occurred: " + ex.getMessage(),
                "credentials");
        }
    }

    private List<String> extractAuthorities(final UserDetails userDetails) {
        return userDetails.getAuthorities().stream()
            .map(Object::toString)
            .toList();
    }

    private List<ProfileDto> fetchAndFilterProfiles(final String email) {
        List<ProfileDto> profiles = findProfile.findByAccountEmail(email);
        return profiles.stream()
            .filter(profile -> !profile.isDeleted())
            .filter(profile -> !profile.profileType().equals(ADMIN))
            .toList();
    }

    private Optional<String> extractRefreshToken(final HttpServletRequest request) {
        return jwtUtil.extractRefreshToken(request);
    }
}