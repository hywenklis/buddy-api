package com.buddy.api.utils;

import com.buddy.api.domains.valueobjects.EmailAddress;
import org.apache.commons.lang3.RandomStringUtils;

public class RandomEmailUtils {
    public static String generateValidEmail() {
        return RandomStringUtils.secure().nextAlphabetic(10) + "@example.com";
    }

    public static EmailAddress generateValidEmailAddress() {
        return new EmailAddress(generateValidEmail());
    }
}
