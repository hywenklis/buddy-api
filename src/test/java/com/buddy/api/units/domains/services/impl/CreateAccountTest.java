package com.buddy.api.units.domains.services.impl;

import static com.buddy.api.builders.account.AccountBuilder.validAccountDto;
import static com.buddy.api.builders.account.AccountBuilder.validAccountEntity;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.buddy.api.commons.configurations.cache.TokenManager;
import com.buddy.api.commons.exceptions.EmailAlreadyRegisteredException;
import com.buddy.api.domains.account.dtos.AccountDto;
import com.buddy.api.domains.account.email.services.EmailSender;
import com.buddy.api.domains.account.entities.AccountEntity;
import com.buddy.api.domains.account.mappers.AccountMapper;
import com.buddy.api.domains.account.repositories.AccountRepository;
import com.buddy.api.domains.account.services.impl.CreateAccountImpl;
import com.buddy.api.domains.account.services.impl.CreateAccountImpl.AccountCreatedEvent;
import com.buddy.api.units.UnitTestAbstract;
import java.util.UUID;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.crypto.password.PasswordEncoder;

class CreateAccountTest extends UnitTestAbstract {
    @Mock
    private AccountRepository accountRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @Mock
    private TokenManager tokenManager;

    @Mock
    private EmailSender emailSender;

    @Spy
    private AccountMapper accountMapper = Mappers.getMapper(AccountMapper.class);

    @InjectMocks
    CreateAccountImpl createAccount;

    @Test
    @DisplayName("Should create account and publish AccountCreatedEvent")
    void should_create_account() {
        final var encryptedPassword = RandomStringUtils.secure().nextAlphanumeric(10);

        final AccountDto accountDto = validAccountDto().build();

        final AccountEntity accountEntity = validAccountEntity()
            .email(accountDto.email())
            .password(encryptedPassword)
            .phoneNumber(accountDto.phoneNumber())
            .termsOfUserConsent(accountDto.termsOfUserConsent())
            .build();

        final UUID savedId = UUID.randomUUID();
        final AccountEntity savedEntity = validAccountEntity()
            .accountId(savedId)
            .email(accountDto.email())
            .password(encryptedPassword)
            .phoneNumber(accountDto.phoneNumber())
            .termsOfUserConsent(accountDto.termsOfUserConsent())
            .build();

        when(accountRepository.existsByEmail(accountDto.email())).thenReturn(false);
        when(passwordEncoder.encode(accountDto.password())).thenReturn(encryptedPassword);
        when(accountRepository.save(accountEntity)).thenReturn(savedEntity);

        createAccount.create(accountDto);

        var accountEntityCaptor = ArgumentCaptor.forClass(AccountEntity.class);

        verify(passwordEncoder, times(1)).encode(accountDto.password());
        verify(accountRepository, times(1))
            .save(accountEntityCaptor.capture());
        verify(eventPublisher, times(1))
            .publishEvent(new AccountCreatedEvent(savedId, accountDto.email()));

        assertThat(accountEntity)
            .usingRecursiveComparison()
            .isEqualTo(accountEntityCaptor.getValue());

        assertThat(accountEntityCaptor.getValue().getAccountId()).isNull();
    }

    @Test
    @DisplayName("Should dispatch verification email when handling AccountCreatedEvent")
    void should_dispatch_verification_email_on_account_created_event() {
        final UUID accountId = UUID.randomUUID();
        final AccountDto accountDto = validAccountDto().build();
        final String token = UUID.randomUUID().toString();
        final var event = new AccountCreatedEvent(accountId, accountDto.email());

        when(tokenManager.generateAndStoreToken(accountDto.email().value())).thenReturn(token);

        createAccount.handleAccountCreated(event);

        verify(tokenManager, times(1)).generateAndStoreToken(accountDto.email().value());
        verify(emailSender, times(1))
            .dispatchVerificationEmail(accountId, accountDto.email().value(), token);
    }

    @Test
    @DisplayName("Should not create account when email is already in database")
    void should_not_create_account_when_email_is_already_in_database() {
        final AccountDto accountDto = validAccountDto().build();

        when(accountRepository.existsByEmail(accountDto.email())).thenReturn(true);

        assertThatThrownBy(() -> createAccount.create(accountDto))
            .isInstanceOf(EmailAlreadyRegisteredException.class)
            .hasMessage("Account email already registered")
            .extracting("fieldName")
            .isEqualTo("email");

        verify(accountRepository, times(0)).save(any());
        verifyNoInteractions(eventPublisher);
    }
}
