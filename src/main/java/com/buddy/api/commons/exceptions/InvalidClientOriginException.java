package com.buddy.api.commons.exceptions;

import java.io.Serial;
import org.springframework.http.HttpStatus;

public class InvalidClientOriginException extends DomainException {

    @Serial
    private static final long serialVersionUID = -8514329596334599650L;

    public InvalidClientOriginException(final String message) {
        super(message, "origin", HttpStatus.BAD_REQUEST, null);
    }
}
