package com.buddy.api.commons.validation;

import com.buddy.api.commons.exceptions.DomainException;
import com.buddy.api.web.advice.error.ErrorDetails;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BooleanSupplier;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ValidationCollector implements Validator {

    private final List<ErrorDetails> errors = new ArrayList<>();

    @Override
    public <T extends DomainException> void validate(final BooleanSupplier condition,
                                                     final T exception) {
        if (condition.getAsBoolean()) {
            errors.addAll(exception.getErrors());
        }
    }

    @Override
    public void throwIfErrors() {
        if (!errors.isEmpty()) {
            List<ErrorDetails> currentErrors = new ArrayList<>(errors);
            errors.clear();
            throw new DomainException(currentErrors);
        }
    }
}
