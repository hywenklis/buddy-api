package com.buddy.api.commons.exceptions;

import java.io.Serial;
import org.springframework.http.HttpStatus;

public class UnauthorizedEntityAccessException extends DomainException {

    @Serial
    private static final long serialVersionUID = -5442704791219090423L;

    public UnauthorizedEntityAccessException(final String message) {
        super(message, "authorization", HttpStatus.FORBIDDEN, new RuntimeException(message));
    }
}
