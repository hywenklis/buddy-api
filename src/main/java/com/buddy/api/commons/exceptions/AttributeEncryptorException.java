package com.buddy.api.commons.exceptions;

import java.io.Serial;
import org.springframework.http.HttpStatus;

public class AttributeEncryptorException extends DomainException {

    @Serial
    private static final long serialVersionUID = -4264901419534666561L;

    public AttributeEncryptorException(final String fieldName,
                                       final String message,
                                       final Throwable cause
    ) {
        super(message, fieldName, HttpStatus.INTERNAL_SERVER_ERROR, cause);
    }
}
