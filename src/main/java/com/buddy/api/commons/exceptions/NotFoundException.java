package com.buddy.api.commons.exceptions;

import java.io.Serial;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class NotFoundException extends ValidationException {

    @Serial
    private static final long serialVersionUID = -7819576276216578948L;

    public NotFoundException(final String fieldName, final String message) {
        super(message, fieldName, HttpStatus.NOT_FOUND);
    }
}
