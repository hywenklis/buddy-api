package com.buddy.api.commons.exceptions;

import java.io.Serial;
import org.springframework.http.HttpStatus;

public class AccountBlockedException extends DomainException {

    @Serial
    private static final long serialVersionUID = -4523123456789012345L;

    public AccountBlockedException(final String fieldName, final String message) {
        super(message, fieldName, HttpStatus.FORBIDDEN, null);
    }
}
