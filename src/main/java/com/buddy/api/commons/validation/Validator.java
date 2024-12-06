package com.buddy.api.commons.validation;

import com.buddy.api.commons.exceptions.DomainException;
import java.util.function.BooleanSupplier;

public interface Validator {
    <T extends DomainException> void validate(BooleanSupplier condition, T exception);

    void throwIfErrors();
}
