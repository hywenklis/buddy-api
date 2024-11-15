package com.buddy.api.commons.exceptions;

import java.io.Serial;
import java.io.Serializable;
import lombok.Getter;

@Getter
public class EmailAlreadyRegisteredException extends RuntimeException implements Serializable {
    @Serial
    private static final long serialVersionUID = 4144602870873470711L;

    private final String fieldName;

    public EmailAlreadyRegisteredException(final String message, final String fieldName) {
        super(message);
        this.fieldName = fieldName;
    }
}
