package com.buddy.api.utils;

import java.util.Random;

public class RandomStringUtils {
    public static final String ALPHABET = "abcdefghijklmnopqrstuvwxyz";
    public static final String DIGITS = "01234567890";
    private static final Random RANDOM = new Random();

    public static String generateRandomString(final Integer length, final String alphabet) {
        StringBuilder sb = new StringBuilder();

        for (int i = 0; i < length; i++) {
            sb.append(alphabet.charAt(RANDOM.nextInt(alphabet.length())));
        }

        return sb.toString();
    }

    public static String generateRandomPassword() {
        return generateRandomString(10, ALPHABET);
    }

    public static String generateRandomPhoneNumber() {
        return generateRandomString(9, DIGITS);
    }
}
