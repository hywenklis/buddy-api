package com.buddy.api.utils;

import static com.buddy.api.utils.RandomStringUtils.ALPHABET;
import static com.buddy.api.utils.RandomStringUtils.generateRandomString;

import com.buddy.api.domains.valueobjects.EmailAddress;

public class RandomEmailUtils {
    public static String generateValidEmail() {
        return generateRandomString(10, ALPHABET) + "@example.com";
    }

    public static EmailAddress generateValidEmailAddress() {
        return new EmailAddress(generateValidEmail());
    }
}
