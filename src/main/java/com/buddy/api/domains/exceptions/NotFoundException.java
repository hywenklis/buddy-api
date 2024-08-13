package com.buddy.api.domains.exceptions;

import java.io.Serial;
import java.io.Serializable;
import lombok.Getter;

@Getter
public class NotFoundException extends RuntimeException implements Serializable {

    @Serial
    private static final long serialVersionUID = -7819576276216578948L;

    private final String fieldName;

    public NotFoundException(String fieldName, String message) {
        super(message);
        this.fieldName = fieldName;
    }
}
