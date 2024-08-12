package com.buddy.api.commons.exceptions;

import lombok.Getter;

@Getter
public class PetSearchException extends RuntimeException {
    private final String fieldName;

    public PetSearchException(String fieldName, String message) {
        super(message);
        this.fieldName = fieldName;
    }

    public PetSearchException(String fieldName, String message, Throwable cause) {
        super(message, cause);
        this.fieldName = fieldName;
    }
}
