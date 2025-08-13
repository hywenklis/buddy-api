package com.buddy.api.units.domains.services.impls;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.buddy.api.builders.account.AccountBuilder;
import com.buddy.api.domains.account.dtos.AccountDto;
import com.buddy.api.domains.account.entities.AccountEntity;
import com.buddy.api.domains.account.mappers.AccountMapper;
import com.buddy.api.domains.account.repositories.AccountRepository;
import com.buddy.api.domains.account.services.FindAccount;
import com.buddy.api.domains.account.services.impl.UpdateAccountImpl;
import com.buddy.api.units.UnitTestAbstract;
import java.time.LocalDateTime;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.stream.Stream;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mapstruct.factory.Mappers;
import org.mockito.ArgumentCaptor;
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
        final Consumer<AccountEntity> assertion) {
        final AccountDto accountDto = AccountBuilder.validAccountDto().build();

        when(findAccount.findByEmail(accountDto.email().value())).thenReturn(accountDto);

        action.accept(updateAccount, accountDto.email().value());

        verify(findAccount, times(1)).findByEmail(accountDto.email().value());
        verify(accountMapper, times(1)).toAccountEntityForUpdate(accountDto);

        final ArgumentCaptor<AccountEntity> entityCaptor =
            ArgumentCaptor.forClass(AccountEntity.class);
        verify(accountRepository, times(1)).save(entityCaptor.capture());

        final AccountEntity savedEntity = entityCaptor.getValue();

        assertThat(savedEntity).isNotNull();
        assertThat(savedEntity.getEmail()).isEqualTo(accountDto.email());
        assertThat(savedEntity.getPassword()).isEqualTo(accountDto.password());
        assertThat(savedEntity.getPhoneNumber()).isEqualTo(accountDto.phoneNumber());
        assertThat(savedEntity.getTermsOfUserConsent()).isEqualTo(accountDto.termsOfUserConsent());

        assertion.accept(savedEntity);
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

                (Consumer<AccountEntity>)
                    entity -> assertThat(entity.getLastLogin()).isEqualTo(lastLogin)
            ),
            Arguments.of(
                "Update isVerified status",
                (BiConsumer<UpdateAccountImpl, String>) (
                    service,
                    email
                ) -> service.updateIsVerified(email, isVerified),

                (Consumer<AccountEntity>)
                    entity -> assertThat(entity.getIsVerified()).isEqualTo(isVerified)
            )
        );
    }
}
