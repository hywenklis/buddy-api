package com.buddy.api.commons.exceptions;

import com.buddy.api.domains.validation.dtos.ValidationDetailsDto;
import java.io.Serial;
import java.util.List;

public class DomainValidationException extends RuntimeException {

    @Serial
    private static final long serialVersionUID = 9206141270299873008L;
    private final List<ValidationDetailsDto> errors;

    public DomainValidationException(final List<ValidationDetailsDto> errors) {
        super("validation failed");
        this.errors = errors;
    }
}
