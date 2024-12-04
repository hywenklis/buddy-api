package com.buddy.api.commons.exceptions;

import java.io.Serial;
import org.springframework.http.HttpStatus;

public class InvalidProfileTypeException extends DomainException {

    @Serial
    private static final long serialVersionUID = 8182926263647923997L;

    public InvalidProfileTypeException(final String message) {
        super(message, "profileType", HttpStatus.FORBIDDEN);
    }
}
