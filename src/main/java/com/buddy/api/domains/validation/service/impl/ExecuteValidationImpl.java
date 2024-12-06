package com.buddy.api.domains.validation.service.impl;

import com.buddy.api.commons.exceptions.DomainValidationException;
import com.buddy.api.domains.validation.dtos.ValidationDetailsDto;
import com.buddy.api.domains.validation.service.ExecuteValidation;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public class ExecuteValidationImpl<T> implements ExecuteValidation<T> {
    private final List<ValidationDetailsDto> errors = new ArrayList<>();

    @Override
    public ExecuteValidation<T> validate(final Supplier<List<ValidationDetailsDto>> errors) {
        this.errors.addAll(errors.get());
        return this;
    }

    @Override
    public T andThen(final Supplier<T> execution) {
        if (errors.isEmpty()) {
            return execution.get();
        }

        throw new DomainValidationException(errors);
    }
}
