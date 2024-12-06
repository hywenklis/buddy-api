package com.buddy.api.utils;

import static com.buddy.api.utils.RandomStringUtils.generateRandomString;

import com.buddy.api.domains.validation.dtos.ValidationDetailsDto;

public class RandomValidationDetailsUtils {
    public static ValidationDetailsDto generateRandomValidationDetailsDto() {
        return new ValidationDetailsDto(
            generateRandomString(10),
            generateRandomString(6)
        );
    }
}
