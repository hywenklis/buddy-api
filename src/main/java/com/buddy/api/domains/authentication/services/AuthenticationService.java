package com.buddy.api.domains.authentication.services;

import static com.buddy.api.domains.profile.enums.ProfileTypeEnum.ADMIN;

import com.buddy.api.commons.configuration.jwt.JwtUtil;
import com.buddy.api.domains.account.dtos.AccountDto;
import com.buddy.api.domains.account.services.FindAccount;
import com.buddy.api.domains.account.services.UpdateAccount;
import com.buddy.api.domains.authentication.dtos.AuthDto;
import com.buddy.api.domains.profile.dtos.ProfileDto;
import com.buddy.api.domains.profile.services.FindProfile;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthenticationService {

    private final AuthenticationManager authenticationManager;
    private final FindAccount findAccount;
    private final FindProfile findProfile;
    private final JwtUtil jwtUtil;
    private final UpdateAccount updateAccount;

    public AuthDto authenticate(final AuthDto authDto) {
        authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(authDto.email(), authDto.password())
        );

        AccountDto account = findAccount.findByEmail(authDto.email());
        List<ProfileDto> profiles = findProfile.findByAccountId(account.accountId());

        List<ProfileDto> profilesDto = profiles.stream()
            .filter(profile -> !profile.isDeleted())
            .filter(profile -> !profile.profileType().equals(ADMIN))
            .toList();

        List<String> profileAuthorities = profiles.stream()
            .filter(profile -> !profile.isDeleted())
            .map(profile -> profile.profileType().name())
            .toList();

        String accessToken = jwtUtil.generateAccessToken(account.accountId(), profileAuthorities);
        String refreshToken = jwtUtil.generateRefreshToken(account.accountId());

        updateAccount.updateLastLogin(account.accountId(), LocalDateTime.now());

        // TODO: talvez extrair para o mapper struct?
        return new AuthDto(
            authDto.email(),
            authDto.password(),
            profilesDto,
            accessToken,
            refreshToken
        );
    }
}
