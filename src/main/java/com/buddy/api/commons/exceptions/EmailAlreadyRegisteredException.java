package com.buddy.api.commons.exceptions;

import java.io.Serial;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class EmailAlreadyRegisteredException extends DomainException {

    @Serial
    private static final long serialVersionUID = 4067260310533491061L;

    public EmailAlreadyRegisteredException(final String message, final String fieldName) {
        super(message, fieldName, HttpStatus.BAD_REQUEST, null);
    }
}
