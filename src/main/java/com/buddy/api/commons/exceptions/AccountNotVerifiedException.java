package com.buddy.api.commons.exceptions;

import java.io.Serial;
import org.springframework.http.HttpStatus;

public class AccountNotVerifiedException extends DomainException {

    @Serial
    private static final long serialVersionUID = 8765432109876543210L;

    public AccountNotVerifiedException(final String fieldName, final String message) {
        super(message, fieldName, HttpStatus.FORBIDDEN, null);
    }
}
