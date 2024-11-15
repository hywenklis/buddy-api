package com.buddy.api.commons.exceptions;

import java.io.Serial;
import lombok.Getter;

@Getter
public class EmailAlreadyRegisteredException extends DomainException {

    @Serial
    private static final long serialVersionUID = 4067260310533491061L;

    public EmailAlreadyRegisteredException(final String message, final String fieldName) {
        super(message, fieldName);
    }
}
