package com.buddy.api.units.domains.services.impls;

import static org.assertj.core.api.AssertionsForClassTypes.assertThatNoException;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

import com.buddy.api.builders.account.AccountBuilder;
import com.buddy.api.commons.exceptions.AccountAlreadyVerifiedException;
import com.buddy.api.commons.exceptions.AuthenticationException;
import com.buddy.api.domains.account.dtos.AccountDto;
import com.buddy.api.domains.account.email.services.impls.AccountValidatorImpl;
import com.buddy.api.domains.valueobjects.EmailAddress;
import com.buddy.api.units.UnitTestAbstract;
import com.buddy.api.utils.RandomEmailUtils;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class AccountValidatorTest extends UnitTestAbstract {

    private AccountValidatorImpl accountValidator;

    private AccountDto unverifiedAccount;
    private AccountDto verifiedAccount;
    private UUID accountId;
    private String tokenEmail;

    @BeforeEach
    void setUp() {
        accountValidator = new AccountValidatorImpl();
        EmailAddress emailAddress = RandomEmailUtils.generateValidEmailAddress();
        accountId = UUID.randomUUID();
        tokenEmail = emailAddress.value();
        unverifiedAccount = AccountBuilder.validAccountDto()
            .email(emailAddress)
            .accountId(accountId)
            .isVerified(false)
            .build();

        verifiedAccount = AccountBuilder.validAccountDto()
            .email(emailAddress)
            .accountId(accountId)
            .isVerified(true)
            .build();
    }

    @Nested
    @DisplayName("Tests for validateAccountNotVerified method")
    class ValidateAccountNotVerifiedTests {

        @Test
        @DisplayName("Should pass when account is not verified")
        void should_pass_when_account_not_verified() {
            assertThatNoException().isThrownBy(
                () -> accountValidator.validateAccountNotVerified(unverifiedAccount));
        }

        @Test
        @DisplayName("Should throw AccountAlreadyVerifiedException when account is verified")
        void should_throw_account_already_verified_exception() {
            assertThatThrownBy(() -> accountValidator.validateAccountNotVerified(verifiedAccount))
                .isInstanceOf(AccountAlreadyVerifiedException.class)
                .hasMessageContaining("This account is already verified");
        }
    }

    @Nested
    @DisplayName("Tests for validateTokenMatchesAccount method")
    class ValidateTokenMatchesAccountTests {

        @Test
        @DisplayName("Should pass when token email matches account email")
        void should_pass_when_token_email_matches_account_email() {
            assertThatNoException().isThrownBy(() -> accountValidator.validateTokenMatchesAccount(
                unverifiedAccount, tokenEmail, accountId));
        }

        @Test
        @DisplayName("Should throw AuthenticationException when "
            + "token email does not match account email")
        void should_throw_authentication_exception_when_emails_do_not_match() {
            String differentEmail = "different@example.com";

            assertThatThrownBy(() -> accountValidator.validateTokenMatchesAccount(
                unverifiedAccount, differentEmail, accountId))
                .isInstanceOf(AuthenticationException.class)
                .hasMessageContaining("Token does not belong to the authenticated user");
        }

        @Test
        @DisplayName("Should throw IllegalArgumentException when emailFromToken is null")
        void should_throw_illegal_argument_exception_when_email_from_token_null() {
            assertThatThrownBy(
                () -> accountValidator.validateTokenMatchesAccount(unverifiedAccount, null,
                    accountId))
                .isInstanceOf(AuthenticationException.class)
                .hasMessage("Token does not belong to the authenticated user");
        }
    }
}
