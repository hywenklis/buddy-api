package com.buddy.api.domains.configuration.annotations;

import com.buddy.api.domains.configuration.annotations.validators.UniqueValidator;
import com.buddy.api.domains.enums.UniqueType;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Documented
@Constraint(validatedBy = UniqueValidator.class)
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface Unique {

    String message() default "Value must be unique";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

    UniqueType value();
}
