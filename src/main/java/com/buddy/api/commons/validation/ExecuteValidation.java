package com.buddy.api.commons.validation;

import java.util.function.Supplier;

public interface ExecuteValidation<T> {

    ExecuteValidation<T> validate(Validation validation);

    T andThen(Supplier<T> execution);
}
