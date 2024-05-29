package com.buddy.api.domains.exceptions;

import lombok.Getter;

@Getter
public class AlreadyExistsException extends RuntimeException {
    private final String fieldName;

    public AlreadyExistsException(String fieldName, String message) {
        super(message);
        this.fieldName = fieldName;
    }
}
