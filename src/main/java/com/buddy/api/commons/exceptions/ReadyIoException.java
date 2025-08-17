package com.buddy.api.commons.exceptions;

import java.io.Serial;
import org.springframework.http.HttpStatus;

public class ReadyIoException extends DomainException {

    @Serial
    private static final long serialVersionUID = 6262472518759187165L;

    public ReadyIoException(final String message,
                            final String field,
                            final Throwable throwable
    ) {
        super(message, field, HttpStatus.INTERNAL_SERVER_ERROR, throwable);
    }
}
