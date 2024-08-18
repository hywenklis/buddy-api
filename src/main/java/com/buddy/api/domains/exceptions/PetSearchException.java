package com.buddy.api.domains.exceptions;

import java.io.Serial;
import java.io.Serializable;
import lombok.Getter;

@Getter
public class PetSearchException extends RuntimeException implements Serializable {

    @Serial
    private static final long serialVersionUID = 1704349073637945329L;

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
