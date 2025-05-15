package com.buddy.api.commons.exceptions;

import java.io.Serial;
import org.springframework.http.HttpStatus;

public class AuthenticationException extends DomainException {

    @Serial
    private static final long serialVersionUID = 2002171235384025372L;

    public AuthenticationException(final String message, final String field) {
        super(message, field, HttpStatus.UNAUTHORIZED);
    }
}
