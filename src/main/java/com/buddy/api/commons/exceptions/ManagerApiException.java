package com.buddy.api.commons.exceptions;

import java.io.Serial;
import org.springframework.http.HttpStatus;

public class ManagerApiException extends DomainException {

    @Serial
    private static final long serialVersionUID = 2002171235384025372L;

    public ManagerApiException(final String message,
                               final String field,
                               final HttpStatus status,
                               final Throwable cause
    ) {
        super(message, field, status, cause);
    }
}
