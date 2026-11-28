package com.buddy.api.domains.account.services.impl;

import com.buddy.api.commons.configurations.cache.TokenManager;
import com.buddy.api.commons.exceptions.EmailAlreadyRegisteredException;
import com.buddy.api.domains.account.dtos.AccountDto;
import com.buddy.api.domains.account.email.services.EmailSender;
import com.buddy.api.domains.account.mappers.AccountMapper;
import com.buddy.api.domains.account.repositories.AccountRepository;
import com.buddy.api.domains.account.services.CreateAccount;
import com.buddy.api.domains.valueobjects.EmailAddress;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@Service
@RequiredArgsConstructor
public class CreateAccountImpl implements CreateAccount {
    private final AccountRepository accountRepository;
    private final PasswordEncoder passwordEncoder;
    private final AccountMapper accountMapper;
    private final ApplicationEventPublisher eventPublisher;
    private final TokenManager tokenManager;
    private final EmailSender emailSender;

    @Override
    @Transactional
    public void create(final AccountDto accountDto) {
        validateEmailIsNotRegistered(accountDto.email());

        final var accountEntity = accountMapper.toAccountEntity(accountDto);
        accountEntity.setPassword(passwordEncoder.encode(accountDto.password()));

        log.info("Creating account");
        final var savedAccount = accountRepository.save(accountEntity);
        log.info("Account created successfully with ID: {}", savedAccount.getAccountId());

        eventPublisher.publishEvent(
            new AccountCreatedEvent(savedAccount.getAccountId(), savedAccount.getEmail())
        );
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleAccountCreated(final AccountCreatedEvent event) {
        final String email = event.email().value();
        final String token = tokenManager.generateAndStoreToken(email);
        emailSender.dispatchVerificationEmail(event.accountId(), email, token);
    }

    private void validateEmailIsNotRegistered(final EmailAddress email) {
        if (Boolean.TRUE.equals(accountRepository.existsByEmail(email))) {
            throw new EmailAlreadyRegisteredException("Account email already registered", "email");
        }
    }

    public record AccountCreatedEvent(UUID accountId, EmailAddress email) {
    }
}
