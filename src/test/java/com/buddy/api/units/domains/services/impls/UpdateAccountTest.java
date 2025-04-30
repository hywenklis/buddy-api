package com.buddy.api.units.domains.services.impls;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.buddy.api.builders.account.AccountBuilder;
import com.buddy.api.commons.exceptions.NotFoundException;
import com.buddy.api.domains.account.entities.AccountEntity;
import com.buddy.api.domains.account.repositories.AccountRepository;
import com.buddy.api.domains.account.services.impl.UpdateAccountImpl;
import com.buddy.api.domains.valueobjects.EmailAddress;
import com.buddy.api.units.UnitTestAbstract;
import com.buddy.api.utils.RandomEmailUtils;
import java.time.LocalDateTime;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;

class UpdateAccountTest extends UnitTestAbstract {

    @Mock
    private AccountRepository accountRepository;

    @InjectMocks
    private UpdateAccountImpl updateAccount;

    @Test
    @DisplayName("Should update last login successfully when account exists")
    void should_update_last_login_successfully() {
        LocalDateTime lastLogin = LocalDateTime.now();
        var account = AccountBuilder.validAccountDto().build();
        var accountEntity = AccountBuilder.validAccountEntity()
            .email(account.email())
            .password(account.password())
            .phoneNumber(account.phoneNumber())
            .termsOfUserConsent(account.termsOfUserConsent())
            .build();

        when(accountRepository.findByEmail(account.email()))
            .thenReturn(Optional.of(accountEntity));
        when(accountRepository.save(accountEntity))
            .thenReturn(accountEntity);

        updateAccount.updateLastLogin(account.email().value(), lastLogin);

        verify(accountRepository).findByEmail(account.email());

        ArgumentCaptor<AccountEntity> entityCaptor = ArgumentCaptor.forClass(AccountEntity.class);
        verify(accountRepository, times(1)).save(entityCaptor.capture());

        AccountEntity savedEntity = entityCaptor.getValue();
        assertThat(savedEntity).isNotNull();
        assertThat(savedEntity.getLastLogin()).isEqualTo(lastLogin);
        assertThat(savedEntity.getEmail()).isEqualTo(account.email());
        assertThat(savedEntity.getPassword()).isEqualTo(account.password());
        assertThat(savedEntity.getPhoneNumber()).isEqualTo(account.phoneNumber());
        assertThat(savedEntity.getTermsOfUserConsent()).isEqualTo(account.termsOfUserConsent());
    }

    @Test
    @DisplayName("Should throw NotFoundException when account does not exist")
    void should_throw_not_found_exception_when_account_does_not_exist() {
        var email = RandomEmailUtils.generateValidEmail();
        var lastLogin = LocalDateTime.now();
        when(accountRepository.findByEmail(new EmailAddress(email)))
            .thenReturn(Optional.empty());

        assertThatThrownBy(() -> updateAccount.updateLastLogin(email, lastLogin))
            .isInstanceOf(NotFoundException.class)
            .hasMessage("Account not found");

        verify(accountRepository, times(1)).findByEmail(new EmailAddress(email));
        verify(accountRepository, times(0)).save(any(AccountEntity.class));
    }
}
