package com.buddy.api.commons.exceptions;

import java.io.Serial;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class DomainException extends RuntimeException {

    @Serial
    private static final long serialVersionUID = 3389907679696634663L;

    private final String fieldName;
    private final HttpStatus httpStatus;

    public DomainException(final String message,
                           final String fieldName,
                           final HttpStatus httpStatus) {
        super(message);
        this.fieldName = fieldName;
        this.httpStatus = httpStatus;
    }
}
