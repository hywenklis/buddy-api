package com.buddy.api.domains.exceptions;

import java.io.Serial;
import java.io.Serializable;
import lombok.Getter;

@Getter
public class InvalidSortFieldException extends RuntimeException implements Serializable {

    @Serial
    private static final long serialVersionUID = -517624748264197788L;

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
