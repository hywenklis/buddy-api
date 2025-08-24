package com.buddy.api.commons.exceptions;

import java.io.Serial;
import org.springframework.http.HttpStatus;

public class AccountAlreadyVerifiedException extends DomainException {

    @Serial
    private static final long serialVersionUID = 1072907289249447384L;

    public AccountAlreadyVerifiedException(final String fieldName, final String message) {
        super(message, fieldName, HttpStatus.CONFLICT, null);
    }
}
