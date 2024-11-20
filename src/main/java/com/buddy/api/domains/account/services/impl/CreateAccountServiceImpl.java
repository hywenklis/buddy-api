package com.buddy.api.domains.account.services.impl;

import com.buddy.api.commons.exceptions.EmailAlreadyRegisteredException;
import com.buddy.api.domains.account.dtos.AccountDto;
import com.buddy.api.domains.account.repository.AccountRepository;
import com.buddy.api.domains.account.services.CreateAccountService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CreateAccountServiceImpl implements CreateAccountService {
    private final AccountRepository accountRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void create(final AccountDto accountDto) {
        validateEmailIsNotRegistered(accountDto.email());

        var accountEntity = accountDto.toAccountEntity();

        accountEntity.setPassword(passwordEncoder.encode(accountDto.password()));
        accountRepository.save(accountEntity);
    }

    private void validateEmailIsNotRegistered(final String email) {
        if (accountRepository.existsByEmail(email)) {
            throw new EmailAlreadyRegisteredException("Account email already registered", "email");
        }
    }
}
