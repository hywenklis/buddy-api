package com.buddy.api.commons.exceptions;

import java.io.Serial;
import org.springframework.http.HttpStatus;

public class TooManyRequestsException extends DomainException {

    @Serial
    private static final long serialVersionUID = 2702355471027463717L;

    public TooManyRequestsException(final String message) {
        super(message, "rateLimit", HttpStatus.TOO_MANY_REQUESTS, null);
    }

}
