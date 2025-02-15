package com.buddy.api.units.domains.services.impls;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import com.buddy.api.domains.account.repository.AccountRepository;
import com.buddy.api.domains.account.services.impl.FindAccountImpl;
import com.buddy.api.units.UnitTestAbstract;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

class FindAccountTest extends UnitTestAbstract {

    @Mock
    private AccountRepository accountRepository;

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
}
