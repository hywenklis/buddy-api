package com.buddy.api.units.domains.services.impls;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.Mockito.when;

import com.buddy.api.builders.account.AccountBuilder;
import com.buddy.api.commons.exceptions.AccountUnavailableException;
import com.buddy.api.commons.exceptions.NotFoundException;
import com.buddy.api.domains.account.dtos.AccountDto;
import com.buddy.api.domains.account.mappers.AccountMapper;
import com.buddy.api.domains.account.repositories.AccountRepository;
import com.buddy.api.domains.account.services.impl.FindAccountImpl;
import com.buddy.api.domains.valueobjects.EmailAddress;
import com.buddy.api.units.UnitTestAbstract;
import com.buddy.api.utils.RandomEmailUtils;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;

class FindAccountTest extends UnitTestAbstract {

    @Mock
    private AccountRepository accountRepository;

    @Spy
    private AccountMapper accountMapper = Mappers.getMapper(AccountMapper.class);

    @InjectMocks
    private FindAccountImpl findAccount;

    @Test
    @DisplayName("Should inform when account with given account ID does not exists")
    void should_inform_when_account_with_given_account_id_does_not_exists() {
        var accountId = UUID.randomUUID();
        when(accountRepository.existsByAccountIdAndIsDeleted(accountId, false))
            .thenReturn(false);
        assertThat(findAccount.existsById(accountId)).isFalse();
    }

    @Test
    @DisplayName("Should inform when account with given account ID exists")
    void should_inform_when_account_with_given_account_id_exists() {
        var accountId = UUID.randomUUID();

        when(accountRepository.existsByAccountIdAndIsDeleted(accountId, false))
            .thenReturn(true);

        assertThat(findAccount.existsById(accountId)).isTrue();
    }

    @Test
    @DisplayName("Should find account by email and return AccountDto when account is active")
    void should_find_account_by_email_successfully() {
        var account = AccountBuilder.validAccountDto()
            .isDeleted(false)
            .isBlocked(false)
            .isVerified(false)
            .lastLogin(LocalDateTime.now())
            .build();
        var accountEntity = AccountBuilder.validAccountEntity()
            .email(account.email())
            .password(account.password())
            .phoneNumber(account.phoneNumber())
            .termsOfUserConsent(account.termsOfUserConsent())
            .isDeleted(account.isDeleted())
            .isBlocked(account.isBlocked())
            .lastLogin(account.lastLogin())
            .build();

        when(accountRepository.findByEmail(account.email()))
            .thenReturn(Optional.of(accountEntity));

        AccountDto result = findAccount.findByEmail(account.email().value());
        assertThat(result).usingRecursiveComparison().isEqualTo(account);
    }

    @Test
    @DisplayName("Should throw NotFoundException when account is not found")
    void should_throw_not_found_exception_when_account_not_found() {
        var email = RandomEmailUtils.generateValidEmail();
        when(accountRepository.findByEmail(new EmailAddress(email)))
            .thenReturn(Optional.empty());

        assertThatThrownBy(() -> findAccount.findByEmail(email))
            .isInstanceOf(NotFoundException.class)
            .hasMessage("Account not found");
    }

    @Test
    @DisplayName("Should throw AccountUnavailableException when account is blocked")
    void should_throw_account_unavailable_exception_when_account_blocked() {
        var account = AccountBuilder.validAccountDto().build();
        var accountEntity = AccountBuilder.validAccountEntity()
            .email(account.email())
            .password(account.password())
            .phoneNumber(account.phoneNumber())
            .termsOfUserConsent(account.termsOfUserConsent())
            .isBlocked(true)
            .isDeleted(false)
            .build();

        when(accountRepository.findByEmail(account.email()))
            .thenReturn(Optional.of(accountEntity));

        assertThatThrownBy(() -> findAccount.findByEmail(account.email().value()))
            .isInstanceOf(AccountUnavailableException.class)
            .hasMessage("Account is not available");
    }

    @Test
    @DisplayName("Should throw AccountUnavailableException when account is deleted")
    void should_throw_account_unavailable_exception_when_account_deleted() {
        var account = AccountBuilder.validAccountDto().build();
        var accountEntity = AccountBuilder.validAccountEntity()
            .email(account.email())
            .password(account.password())
            .phoneNumber(account.phoneNumber())
            .termsOfUserConsent(account.termsOfUserConsent())
            .isBlocked(false)
            .isDeleted(true)
            .build();

        when(accountRepository.findByEmail(account.email()))
            .thenReturn(Optional.of(accountEntity));

        assertThatThrownBy(() -> findAccount.findByEmail(account.email().value()))
            .isInstanceOf(AccountUnavailableException.class)
            .hasMessage("Account is not available");
    }
}
