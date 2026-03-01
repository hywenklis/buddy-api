package com.buddy.api.components;

import com.buddy.api.builders.account.AccountBuilder;
import com.buddy.api.builders.profile.ProfileBuilder;
import com.buddy.api.commons.configurations.security.jwt.JwtUtil;
import com.buddy.api.domains.account.entities.AccountEntity;
import com.buddy.api.domains.account.repositories.AccountRepository;
import com.buddy.api.domains.profile.entities.ProfileEntity;
import com.buddy.api.domains.profile.enums.ProfileTypeEnum;
import com.buddy.api.domains.profile.repositories.ProfileRepository;
import java.util.List;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class AccountComponent {

    private final AccountRepository accountRepository;
    private final ProfileRepository profileRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    public AccountComponent(final AccountRepository accountRepository,
                            final ProfileRepository profileRepository,
                            final PasswordEncoder passwordEncoder,
                            final JwtUtil jwtUtil
    ) {
        this.accountRepository = accountRepository;
        this.profileRepository = profileRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
    }

    public AuthenticatedTestUser createAndAuthenticateUser() {
        String plainPassword = RandomStringUtils.secure().nextAlphanumeric(12);
        AccountEntity account = AccountBuilder.validAccountEntity()
            .password(passwordEncoder.encode(plainPassword))
            .isVerified(false)
            .build();
        accountRepository.save(account);

        ProfileEntity profile = ProfileBuilder.profileEntity()
            .account(account)
            .profileType(ProfileTypeEnum.USER)
            .build();
        profileRepository.save(profile);

        String jwt = jwtUtil.generateAccessToken(account.getEmail().value(), List.of("ROLE_USER"));

        return new AuthenticatedTestUser(account, jwt, plainPassword);
    }

    public AuthenticatedTestUser createAndAuthenticateAdmin() {
        String plainPassword = RandomStringUtils.secure().nextAlphanumeric(12);
        AccountEntity account = AccountBuilder.validAccountEntity()
            .password(passwordEncoder.encode(plainPassword))
            .isVerified(true)
            .build();
        accountRepository.save(account);

        ProfileEntity profile = ProfileBuilder.profileEntity()
            .account(account)
            .profileType(ProfileTypeEnum.ADMIN)
            .build();
        profileRepository.save(profile);

        String jwt = jwtUtil.generateAccessToken(account.getEmail().value(), List.of("ROLE_ADMIN"));

        return new AuthenticatedTestUser(account, jwt, plainPassword);
    }

    public record AuthenticatedTestUser(AccountEntity account, String jwt, String plainPassword) {
    }
}