package com.buddy.api.units.common.validation.impl;

import static com.buddy.api.utils.RandomValidationDetailsUtils.generateRandomValidationDetailsDto;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.buddy.api.commons.exceptions.DomainValidationException;
import com.buddy.api.commons.validation.dtos.ValidationDetailsDto;
import com.buddy.api.commons.validation.impl.ExecuteValidationImpl;
import com.buddy.api.units.UnitTestAbstract;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class ExecuteValidationTest extends UnitTestAbstract {
    @Test
    @DisplayName("Should execute normally with no validation")
    void should_execute_normally_with_no_validations() {
        final var executeValidation = new ExecuteValidationImpl<Boolean>();
        final var executed = executeValidation.andThen(() -> true);

        assertThat(executed).isTrue();
    }

    @Test
    @DisplayName("Should be immutable")
    void should_be_immutable() {
        final var executeValidation = new ExecuteValidationImpl<Boolean>();
        final var newExecuteValidation = executeValidation.validate(
            () -> List.of(generateRandomValidationDetailsDto())
        );

        var executed = executeValidation.andThen(() -> true);

        assertThat(newExecuteValidation).isNotSameAs(executeValidation);
        assertThat(executed).isTrue();
    }

    @Test
    @DisplayName("Should throw validation exception when validation fails")
    void should_throw_validation_exception_when_validation_fails() {
        final var executeValidation = new ExecuteValidationImpl<Boolean>();

        final var errors = List.of(
            generateRandomValidationDetailsDto(),
            generateRandomValidationDetailsDto()
        );

        assertThatThrownBy(() -> executeValidation
            .validate(() -> errors)
            .andThen(() -> true))
            .isInstanceOf(DomainValidationException.class)
            .hasMessage("validation failed")
            .extracting("errors")
            .isEqualTo(errors);
    }

    @Test
    @DisplayName("Should execute normally when all validation succeeds")
    void should_execute_normally_when_all_validations_succeeds() {
        final var executeValidation = new ExecuteValidationImpl<Boolean>();
        final var executed = executeValidation
            .validate(List::of)
            .andThen(() -> true);

        assertThat(executed).isTrue();
    }

    @Test
    @DisplayName("Should keep track of all failed validations")
    void should_accumulate_validation_errors() {
        final var executeValidation = new ExecuteValidationImpl<Boolean>();

        final var errors = List.of(
            generateRandomValidationDetailsDto(),
            generateRandomValidationDetailsDto()
        );

        final var nextErrors = List.of(
            generateRandomValidationDetailsDto()
        );

        final var allErrors = new ArrayList<ValidationDetailsDto>();
        allErrors.addAll(errors);
        allErrors.addAll(nextErrors);

        assertThatThrownBy(() -> executeValidation
            .validate(() -> errors)
            .validate(() -> nextErrors)
            .andThen(() -> true))
            .extracting("errors")
            .isEqualTo(allErrors);
    }
}
