package com.buddy.api.units.domains.annotations.validators;

import static com.buddy.api.domains.enums.UniqueType.CPF;
import static com.buddy.api.domains.enums.UniqueType.EMAIL;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.buddy.api.domains.configuration.annotations.Unique;
import com.buddy.api.domains.configuration.annotations.validators.UniqueValidator;
import com.buddy.api.domains.enums.UniqueType;
import com.buddy.api.domains.shelter.dtos.ShelterDto;
import com.buddy.api.domains.shelter.services.FindShelter;
import com.buddy.api.units.UnitTestAbstract;
import jakarta.validation.ConstraintValidatorContext;
import java.util.Optional;
import java.util.stream.Stream;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;


class UniqueValidatorTest extends UnitTestAbstract {

    @Mock
    private FindShelter findShelter;

    @Mock
    private ConstraintValidatorContext context;

    @Mock
    private Unique uniqueAnnotation;

    @InjectMocks
    private UniqueValidator uniqueValidator;

    @DisplayName("isValid should return true when value is null or empty")
    @ParameterizedTest
    @MethodSource("provideNullOrEmptyValues")
    void isValid_ShouldReturnTrue_WhenValueIsNullOrEmpty(String value) {
        // When
        boolean result = uniqueValidator.isValid(value, context);

        // Then
        assertThat(result).isTrue();

        // Verify that repository methods are not called
        verifyNoInteractions(findShelter, findShelter);
    }

    @DisplayName("isValid should return true when value is unique in the repository")
    @ParameterizedTest
    @MethodSource("provideUniqueValues")
    void isValid_ShouldReturnTrue_WhenValueIsUnique(UniqueType type, String uniqueValue) {
        // Given
        when(uniqueAnnotation.value()).thenReturn(type);

        // When
        uniqueValidator.initialize(uniqueAnnotation);
        boolean result = uniqueValidator.isValid(uniqueValue, context);

        // Then
        assertThat(result).isTrue();

        // Verify that the correct repository method is called
        verifyRepositoryMethodInvocation(type, uniqueValue, 1);
    }

    @DisplayName("isValid should return false when value is not unique in the repository")
    @ParameterizedTest
    @MethodSource("provideNonUniqueValues")
    void isValid_ShouldReturnFalse_WhenValueIsNotUnique(UniqueType type, String nonUniqueValue) {
        // Given
        var cpf = ShelterDto.builder().cpfResponsible(nonUniqueValue).build();
        var email = ShelterDto.builder().email(nonUniqueValue).build();

        when(uniqueAnnotation.value()).thenReturn(type);

        // Mock repository responses based on type
        mockRepositoryByType(type, nonUniqueValue, cpf, email);

        // When
        uniqueValidator.initialize(uniqueAnnotation);
        boolean result = uniqueValidator.isValid(nonUniqueValue, context);

        // Then
        assertThat(result).isFalse();

        // Verify that the correct repository method is called
        verifyRepositoryMethodInvocation(type, nonUniqueValue, 1);
    }


    private static Stream<Arguments> provideNullOrEmptyValues() {
        return Stream.of(
            Arguments.of((String) null),
            Arguments.of("")
        );
    }

    private static Stream<Arguments> provideNonUniqueValues() {
        return Stream.of(
            Arguments.of(CPF, "nonUniqueCpf"),
            Arguments.of(EMAIL, "nonUniqueEmail")
        );
    }

    private static Stream<Arguments> provideUniqueValues() {
        return Stream.of(
            Arguments.of(CPF, "uniqueCpf"),
            Arguments.of(EMAIL, "uniqueEmail")
        );
    }

    private void mockRepositoryByType(UniqueType type,
                                      String nonUniqueValue,
                                      ShelterDto shelterDtoByCpf,
                                      ShelterDto shelterDtoByEmail) {
        if (type == CPF) {
            when(findShelter.findShelterByCpfResponsible(nonUniqueValue)).thenReturn(
                Optional.of(shelterDtoByCpf));
        } else if (type == EMAIL) {
            when(findShelter.findShelterByEmail(nonUniqueValue)).thenReturn(
                Optional.of(shelterDtoByEmail));
        }
    }

    private void verifyRepositoryMethodInvocation(UniqueType type, String value, int times) {
        switch (type) {
            case CPF:
                verify(findShelter, times(times)).findShelterByCpfResponsible(value);
                break;
            case EMAIL:
                verify(findShelter, times(times)).findShelterByEmail(value);
                break;
            default:
                throw new IllegalArgumentException("Unsupported validation type: " + type);
        }
    }
}
