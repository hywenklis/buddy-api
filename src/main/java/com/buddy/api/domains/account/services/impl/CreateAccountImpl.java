package com.buddy.api.domains.account.services.impl;

import com.buddy.api.commons.exceptions.EmailAlreadyRegisteredException;
import com.buddy.api.domains.account.dtos.AccountDto;
import com.buddy.api.domains.account.mappers.AccountMapper;
import com.buddy.api.domains.account.repositories.AccountRepository;
import com.buddy.api.domains.account.services.CreateAccount;
import com.buddy.api.domains.valueobjects.EmailAddress;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CreateAccountImpl implements CreateAccount {
    private final AccountRepository accountRepository;
    private final PasswordEncoder passwordEncoder;
    private final AccountMapper accountMapper;

    @Override
    @Transactional
    public void create(final AccountDto accountDto) {
        validateEmailIsNotRegistered(accountDto.email());

        var accountEntity = accountMapper.toAccountEntity(accountDto);

        accountEntity.setPassword(passwordEncoder.encode(accountDto.password()));
        accountRepository.save(accountEntity);
    }

    private void validateEmailIsNotRegistered(final EmailAddress email) {
        if (Boolean.TRUE.equals(accountRepository.existsByEmail(email))) {
            throw new EmailAlreadyRegisteredException("Account email already registered", "email");
        }
    }
}
