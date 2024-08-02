package com.buddy.api.domains.configuration.annotations.validators;

import com.buddy.api.domains.configuration.annotations.Unique;
import com.buddy.api.domains.enums.UniqueType;
import com.buddy.api.domains.shelter.services.FindShelter;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class UniqueValidator implements ConstraintValidator<Unique, String> {

    private final FindShelter findShelter;

    private Map<UniqueType, Function<String, Optional<?>>> repositoryMap;
    private UniqueType type;

    @Override
    public void initialize(Unique constraintAnnotation) {
        type = constraintAnnotation.value();

        repositoryMap = Map.of(
            UniqueType.CPF, findShelter::findShelterByCpfResponsible,
            UniqueType.EMAIL, findShelter::findShelterByEmail
        );
    }

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (StringUtils.isEmpty(value)) {
            return true;
        }

        Optional<?> existingEntity = repositoryMap.get(type).apply(value);
        return existingEntity.isEmpty();
    }
}
