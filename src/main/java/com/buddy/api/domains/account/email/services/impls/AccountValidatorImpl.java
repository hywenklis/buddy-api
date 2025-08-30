package com.buddy.api.domains.account.email.services.impls;

import com.buddy.api.commons.exceptions.AccountAlreadyVerifiedException;
import com.buddy.api.commons.exceptions.AuthenticationException;
import com.buddy.api.domains.account.dtos.AccountDto;
import com.buddy.api.domains.account.email.services.AccountValidator;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class AccountValidatorImpl implements AccountValidator {

    @Override
    public void validateAccountNotVerified(final AccountDto account) {
        if (account.isVerified()) {
            throw new AccountAlreadyVerifiedException(
                "account.status",
                "This account is already verified."
            );
        }
    }

    @Override
    public void validateTokenMatchesAccount(final AccountDto account,
                                            final String emailFromToken,
                                            final UUID accountId
    ) {
        if (!account.email().value().equals(emailFromToken)) {
            log.error("Token email does not match authenticated user account={}", accountId);
            throw new AuthenticationException("Token does not belong to the authenticated user",
                "token"
            );
        }
    }
}
