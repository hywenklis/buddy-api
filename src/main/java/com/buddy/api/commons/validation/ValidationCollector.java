package com.buddy.api.commons.validation;

import com.buddy.api.commons.exceptions.ValidationException;
import com.buddy.api.web.advice.error.ErrorDetails;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BooleanSupplier;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

@Component
public class ValidationCollector {
    private final List<ErrorDetails> errors = new ArrayList<>();

    public void addError(final String field,
                         final String message,
                         final HttpStatus httpStatus
    ) {
        errors.add(new ErrorDetails(
            field,
            message,
            httpStatus,
            httpStatus.value(),
            LocalDateTime.now()
        ));
    }

    public <T> void validate(final BooleanSupplier condition,
                             final String field,
                             final String message,
                             final HttpStatus httpStatus
    ) {
        if (condition.getAsBoolean()) {
            addError(field, message, httpStatus);
        }
    }

    public void throwIfErrors() {
        if (!errors.isEmpty()) {
            List<ErrorDetails> currentErrors = new ArrayList<>(errors);
            errors.clear();
            throw new ValidationException(currentErrors);
        }
    }
}
