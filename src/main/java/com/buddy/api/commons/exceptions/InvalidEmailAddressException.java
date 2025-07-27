package com.buddy.api.commons.exceptions;

import java.io.Serial;
import org.springframework.http.HttpStatus;

public class InvalidEmailAddressException extends DomainException {
    @Serial
    private static final long serialVersionUID = 4067260310533491061L;

    public InvalidEmailAddressException(final String message) {
        super(message, "email", HttpStatus.BAD_REQUEST, null);
    }
}
