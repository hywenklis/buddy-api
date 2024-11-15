package com.buddy.api.units.domains.services.impls;

import static com.buddy.api.builders.account.AccountBuilder.validAccountEntity;
import static com.buddy.api.utils.RandomEmailUtils.generateValidEmail;
import static com.buddy.api.utils.RandomStringUtils.generateRandomPassword;
import static com.buddy.api.utils.RandomStringUtils.generateRandomPhoneNumber;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.buddy.api.commons.exceptions.EmailAlreadyRegisteredException;
import com.buddy.api.domains.account.dtos.AccountDto;
import com.buddy.api.domains.account.entities.AccountEntity;
import com.buddy.api.domains.account.repository.AccountRepository;
import com.buddy.api.domains.account.services.impl.CreateAccountServiceImpl;
import com.buddy.api.units.UnitTestAbstract;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.security.crypto.password.PasswordEncoder;

public class CreateAccountServiceTest extends UnitTestAbstract {
    @Mock
    private AccountRepository accountRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    CreateAccountServiceImpl createAccountService;

    private final Boolean termsOfUserConsent = true;

    @Test
    @DisplayName("Should create account")
    void should_create_account() {
        final var email = generateValidEmail();
        final var phoneNumber = generateRandomPhoneNumber();
        final var password = generateRandomPassword();
        final var encryptedPassword = generateRandomPassword();

        final AccountDto accountDto = new AccountDto(
            email,
            phoneNumber,
            password,
            termsOfUserConsent
        );

        final AccountEntity accountEntity = new AccountEntity(
            null,
            email,
            phoneNumber,
            encryptedPassword,
            termsOfUserConsent,
            false,
            false,
            false,
            null,
            null,
            null
        );

        when(accountRepository.findByEmail(email)).thenReturn(Optional.empty());
        when(passwordEncoder.encode(password)).thenReturn(encryptedPassword);
        when(accountRepository.save(accountEntity)).thenReturn(accountEntity);

        createAccountService.create(accountDto);

        verify(passwordEncoder, times(1)).encode(password);
        verify(accountRepository, times(1)).save(accountEntity);
    }

    @Test
    @DisplayName("Should not create account when email is already in database")
    void should_not_create_account_when_email_is_already_in_database() {
        final var email = generateValidEmail();

        final AccountEntity accountEntity = validAccountEntity()
            .accountId(UUID.randomUUID())
            .creationDate(LocalDateTime.now())
            .updatedDate(LocalDateTime.now())
            .build();

        final AccountDto accountDto = new AccountDto(
            email,
            generateRandomPhoneNumber(),
            generateRandomPassword(),
            termsOfUserConsent
        );

        when(accountRepository.findByEmail(email)).thenReturn(Optional.of(accountEntity));

        assertThatThrownBy(() -> createAccountService.create(accountDto))
            .isInstanceOf(EmailAlreadyRegisteredException.class)
            .usingRecursiveComparison()
            .isEqualTo(
                new EmailAlreadyRegisteredException("Account email already registered", "email")
            );

        verify(accountRepository, times(0)).save(any());
    }
}
