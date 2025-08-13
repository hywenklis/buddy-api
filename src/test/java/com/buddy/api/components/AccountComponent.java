package com.buddy.api.components;

import com.buddy.api.builders.account.AccountBuilder;
import com.buddy.api.commons.configurations.security.jwt.JwtUtil;
import com.buddy.api.domains.account.entities.AccountEntity;
import com.buddy.api.domains.account.repositories.AccountRepository;
import java.util.List;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class AccountComponent {

    private final AccountRepository accountRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    public AccountComponent(final AccountRepository accountRepository,
                            final PasswordEncoder passwordEncoder,
                            final JwtUtil jwtUtil
    ) {
        this.accountRepository = accountRepository;
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

        String jwt = jwtUtil.generateAccessToken(account.getEmail().value(), List.of("USER"));

        return new AuthenticatedTestUser(account, jwt, plainPassword);
    }

    public record AuthenticatedTestUser(AccountEntity account, String jwt, String plainPassword) {
    }
}