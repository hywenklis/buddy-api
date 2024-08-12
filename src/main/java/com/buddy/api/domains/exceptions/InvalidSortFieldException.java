package com.buddy.api.domains.exceptions;

import lombok.Getter;

@Getter
public class InvalidSortFieldException extends RuntimeException {
    private final String fieldName;

    public InvalidSortFieldException(String fieldName, String message) {
        super(message);
        this.fieldName = fieldName;
    }

    public InvalidSortFieldException(String fieldName, String message, Throwable cause) {
        super(message, cause);
        this.fieldName = fieldName;
    }
}
