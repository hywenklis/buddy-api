package com.buddy.api.commons.validation;

import com.buddy.api.commons.validation.dtos.ValidationDetailsDto;
import java.util.List;

@FunctionalInterface
public interface Validation {
    List<ValidationDetailsDto> validate();
}
