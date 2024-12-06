package com.buddy.api.commons.validation;

import com.buddy.api.commons.exceptions.ValidationException;

@FunctionalInterface
public interface Validatable<T> {
    void validate(T input) throws ValidationException;
}
