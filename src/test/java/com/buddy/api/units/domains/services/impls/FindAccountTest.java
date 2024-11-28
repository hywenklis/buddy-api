package com.buddy.api.units.domains.services.impls;

import static com.buddy.api.builders.account.AccountBuilder.validAccountEntity;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import com.buddy.api.domains.account.repository.AccountRepository;
import com.buddy.api.domains.account.services.impl.FindAccountImpl;
import com.buddy.api.units.UnitTestAbstract;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

public class FindAccountTest extends UnitTestAbstract {

    @Mock
    private AccountRepository accountRepository;

    @InjectMocks
    private FindAccountImpl findAccount;

    @Test
    @DisplayName("Should inform when account with given account ID does not exists")
    void should_inform_when_account_with_given_account_id_does_not_exists() {
        var accountId = UUID.randomUUID();
        when(accountRepository.findById(accountId)).thenReturn(Optional.empty());
        assertThat(findAccount.existsById(accountId)).isFalse();
    }

    @Test
    @DisplayName("Should inform when account with given account ID exists")
    void should_inform_when_account_with_given_account_id_exists() {
        var accountId = UUID.randomUUID();
        var accountEntity = validAccountEntity()
            .accountId(accountId)
            .isDeleted(false)
            .build();

        when(accountRepository.findById(accountId)).thenReturn(Optional.of(accountEntity));
        assertThat(findAccount.existsById(accountId)).isTrue();
    }

    @Test
    @DisplayName("Should consider that account does not exists when account is marked for deletion")
    void should_consider_that_account_marked_for_deletion_does_not_exists() {
        var accountId = UUID.randomUUID();
        var accountEntity = validAccountEntity()
            .accountId(accountId)
            .isDeleted(true)
            .build();

        when(accountRepository.findById(accountId)).thenReturn(Optional.of(accountEntity));
        assertThat(findAccount.existsById(accountId)).isFalse();
    }
}
