package com.buddy.api.commons.exceptions;

import com.buddy.api.web.advice.error.ErrorDetails;
import java.io.Serial;
import java.util.List;

public class ValidationException extends DomainException {
    @Serial
    private static final long serialVersionUID = 8197931567835125690L;

    public ValidationException(final List<ErrorDetails> errors) {
        super(errors);
    }
}
