package com.buddy.api.commons.exceptions;

import java.io.Serial;
import lombok.Getter;

@Getter
public class DomainException extends RuntimeException {

    @Serial
    private static final long serialVersionUID = 3389907679696634663L;
    private final String fieldName;

    public DomainException(final String message, final String fieldName) {
        super(message);
        this.fieldName = fieldName;
    }

    public DomainException(final String fieldName,
                              final String message,
                              final Throwable cause) {
        super(message, cause);
        this.fieldName = fieldName;
    }
}
