package com.buddy.api.commons.exceptions;

import java.io.Serial;
import org.springframework.http.HttpStatus;

public class ValidationException extends DomainException {
    @Serial
    private static final long serialVersionUID = 8197931567835125690L;

    public ValidationException(final String message,
                               final String field,
                               final HttpStatus httpStatus) {
        super(message, field, httpStatus);
    }
}
