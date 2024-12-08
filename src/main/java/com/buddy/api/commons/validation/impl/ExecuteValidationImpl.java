package com.buddy.api.commons.validation.impl;

import com.buddy.api.commons.exceptions.DomainValidationException;
import com.buddy.api.commons.validation.ExecuteValidation;
import com.buddy.api.commons.validation.dtos.ValidationDetailsDto;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class ExecuteValidationImpl<T> implements ExecuteValidation<T> {
    private final List<ValidationDetailsDto> errors;

    public ExecuteValidationImpl() {
        this.errors = List.of();
    }

    @Override
    public ExecuteValidation<T> validate(final Supplier<List<ValidationDetailsDto>> errors) {
        var allErrors = new ArrayList<ValidationDetailsDto>();
        allErrors.addAll(this.errors);
        allErrors.addAll(errors.get());

        return new ExecuteValidationImpl<>(allErrors);
    }

    @Override
    public T andThen(final Supplier<T> execution) {
        if (errors.isEmpty()) {
            return execution.get();
        }

        throw new DomainValidationException(errors);
    }
}
