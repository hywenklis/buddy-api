package com.buddy.api.commons.exceptions;

import java.io.Serial;
import org.springframework.http.HttpStatus;

public class CacheInitializationException extends DomainException {

    @Serial
    private static final long serialVersionUID = -4482843159688091061L;

    public CacheInitializationException(final String fieldName, final String message) {
        super(message, fieldName, HttpStatus.UNPROCESSABLE_ENTITY, null);
    }
}
