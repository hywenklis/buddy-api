package com.buddy.api.domains.validation.service;

import com.buddy.api.domains.validation.dtos.ValidationDetailsDto;
import java.util.List;
import java.util.function.Supplier;

public interface ExecuteValidation<T> {

    ExecuteValidation<T> validate(Supplier<List<ValidationDetailsDto>> errors);

    T andThen(Supplier<T> execution);
}
