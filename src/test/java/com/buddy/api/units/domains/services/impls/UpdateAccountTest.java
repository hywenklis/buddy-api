package com.buddy.api.units.domains.services.impls;

import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.buddy.api.builders.account.AccountBuilder;
import com.buddy.api.commons.exceptions.AccountUnavailableException;
import com.buddy.api.domains.account.dtos.AccountDto;
import com.buddy.api.domains.account.entities.AccountEntity;
import com.buddy.api.domains.account.mappers.AccountMapper;
import com.buddy.api.domains.account.repositories.AccountRepository;
import com.buddy.api.domains.account.services.FindAccount;
import com.buddy.api.domains.account.services.impl.UpdateAccountImpl;
import com.buddy.api.units.UnitTestAbstract;
import java.time.LocalDateTime;
import java.util.UUID;
import java.util.function.BiConsumer;
import java.util.stream.Stream;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mapstruct.factory.Mappers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;

class UpdateAccountTest extends UnitTestAbstract {
    @Mock
    private AccountRepository accountRepository;

    @Mock
    private FindAccount findAccount;

    @Spy
    private AccountMapper accountMapper = Mappers.getMapper(AccountMapper.class);

    @InjectMocks
    private UpdateAccountImpl updateAccount;

    @DisplayName("Should update account properties successfully")
    @ParameterizedTest(name = "Test case: {0}")
    @MethodSource("updateAccountArgumentsProvider")
    void should_update_account_properties_successfully(
        final String testCaseName,
        final BiConsumer<UpdateAccountImpl, String> action,
        final LocalDateTime lastLogin,
        final Boolean isVerified
    ) {
        final AccountDto accountDto = AccountBuilder.validAccountDto().build();
        final String email = accountDto.email().value();
        final UUID accountId = accountDto.accountId();

        when(findAccount.findByEmail(email)).thenReturn(accountDto);

        if (testCaseName.contains("last login")) {
            when(accountRepository.updateLastLogin(accountId, lastLogin)).thenReturn(1);
        } else {
            when(accountRepository.updateIsVerified(accountId, isVerified)).thenReturn(1);
        }

        action.accept(updateAccount, email);

        verify(findAccount, times(1)).findByEmail(email);

        verify(accountMapper, times(1))
            .toAccountEntityForUpdate(accountDto);

        if (testCaseName.contains("last login")) {
            verify(accountRepository, times(1))
                .updateLastLogin(accountId, lastLogin);
            verify(accountRepository, times(0))
                .updateIsVerified(any(UUID.class), any(Boolean.class));
        } else {
            verify(accountRepository, times(1))
                .updateIsVerified(accountId, isVerified);
            verify(accountRepository, times(0))
                .updateLastLogin(any(UUID.class), any(LocalDateTime.class));
        }

        verify(accountRepository, times(0))
            .save(any(AccountEntity.class));
    }

    private static Stream<Arguments> updateAccountArgumentsProvider() {
        final LocalDateTime lastLogin = LocalDateTime.now();
        final boolean isVerified = true;

        return Stream.of(
            Arguments.of(
                "Update last login",
                (BiConsumer<UpdateAccountImpl, String>) (
                    service,
                    email
                ) -> service.updateLastLogin(email, lastLogin),
                lastLogin,
                null
            ),
            Arguments.of(
                "Update isVerified status",
                (BiConsumer<UpdateAccountImpl, String>) (
                    service,
                    email
                ) -> service.updateIsVerified(email, isVerified),
                null,
                isVerified
            )
        );
    }

    @DisplayName("Should throw AccountUnavailableException "
        + "when account is not valid for updateLastLogin")
    @Test
    void should_throw_account_unavailable_exception_for_update_last_login() {
        final AccountDto accountDto = AccountBuilder.validAccountDto().build();
        final String email = accountDto.email().value();
        final UUID accountId = accountDto.accountId();
        final LocalDateTime now = LocalDateTime.now();

        when(findAccount.findByEmail(email)).thenReturn(accountDto);
        when(accountRepository.updateLastLogin(accountId, now)).thenReturn(0);

        assertThatThrownBy(() -> updateAccount.updateLastLogin(email, now))
            .isInstanceOf(AccountUnavailableException.class)
            .hasMessageContaining("Account is not available");

        verify(findAccount, times(1)).findByEmail(email);

        verify(accountMapper, times(1))
            .toAccountEntityForUpdate(accountDto);

        verify(accountRepository, times(1))
            .updateLastLogin(accountId, now);
    }

    @DisplayName("Should throw AccountUnavailableException "
        + "when account is not valid for updateIsVerified")
    @Test
    void should_throw_account_unavailable_exception_for_update_is_verified() {
        final AccountDto accountDto = AccountBuilder.validAccountDto().build();
        final String email = accountDto.email().value();
        final UUID accountId = accountDto.accountId();

        when(findAccount.findByEmail(email)).thenReturn(accountDto);
        when(accountRepository.updateIsVerified(accountId, true)).thenReturn(0);

        assertThatThrownBy(() -> updateAccount.updateIsVerified(email, true))
            .isInstanceOf(AccountUnavailableException.class)
            .hasMessageContaining("Account is not available");

        verify(findAccount, times(1)).findByEmail(email);

        verify(accountMapper, times(1))
            .toAccountEntityForUpdate(accountDto);

        verify(accountRepository, times(1))
            .updateIsVerified(accountId, true);
    }
}