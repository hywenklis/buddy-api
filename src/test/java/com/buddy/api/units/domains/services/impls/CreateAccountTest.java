package com.buddy.api.units.domains.services.impls;

import static com.buddy.api.builders.account.AccountBuilder.validAccountDto;
import static com.buddy.api.builders.account.AccountBuilder.validAccountEntity;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.buddy.api.commons.exceptions.EmailAlreadyRegisteredException;
import com.buddy.api.domains.account.dtos.AccountDto;
import com.buddy.api.domains.account.entities.AccountEntity;
import com.buddy.api.domains.account.mappers.AccountMapper;
import com.buddy.api.domains.account.repository.AccountRepository;
import com.buddy.api.domains.account.services.impl.CreateAccountImpl;
import com.buddy.api.units.UnitTestAbstract;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.springframework.security.crypto.password.PasswordEncoder;

class CreateAccountTest extends UnitTestAbstract {
    @Mock
    private AccountRepository accountRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Spy
    private AccountMapper accountMapper = Mappers.getMapper(AccountMapper.class);

    @InjectMocks
    CreateAccountImpl createAccount;

    @Test
    @DisplayName("Should create account")
    void should_create_account() {
        final var encryptedPassword = RandomStringUtils.secure().nextAlphanumeric(10);

        final AccountDto accountDto = validAccountDto().build();

        final AccountEntity accountEntity = validAccountEntity()
            .email(accountDto.email())
            .password(encryptedPassword)
            .phoneNumber(accountDto.phoneNumber())
            .termsOfUserConsent(accountDto.termsOfUserConsent())
            .build();

        when(accountRepository.existsByEmail(accountDto.email())).thenReturn(false);
        when(passwordEncoder.encode(accountDto.password())).thenReturn(encryptedPassword);
        when(accountRepository.save(accountEntity)).thenReturn(accountEntity);

        createAccount.create(accountDto);

        var accountEntityCaptor = ArgumentCaptor.forClass(AccountEntity.class);

        verify(passwordEncoder, times(1)).encode(accountDto.password());
        verify(accountRepository, times(1))
            .save(accountEntityCaptor.capture());

        assertThat(accountEntity)
            .usingRecursiveComparison()
            .isEqualTo(accountEntityCaptor.getValue());

        assertThat(accountEntityCaptor.getValue().getAccountId()).isNull();
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
    }
}
