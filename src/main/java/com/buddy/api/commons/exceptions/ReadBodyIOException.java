package com.buddy.api.commons.exceptions;

import org.springframework.http.HttpStatus;

public class ReadBodyIOException extends DomainException {

    public ReadBodyIOException(final String message,
                               final String field,
                               final Throwable throwable
    ) {
        super(message, field, HttpStatus.INTERNAL_SERVER_ERROR, throwable);
    }
}
