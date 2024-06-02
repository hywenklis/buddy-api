package com.buddy.api.utils;

import java.util.Random;

public class RandomEmailUtils {

    private static final Random RANDOM = new Random();

    public static String generateValidEmail() {
        String alphabet = "abcdefghijklmnopqrstuvwxyz";
        StringBuilder sb = new StringBuilder();
        int length = 10;

        for (int i = 0; i < length; i++) {
            sb.append(alphabet.charAt(RANDOM.nextInt(alphabet.length())));
        }

        sb.append("@example.com");
        return sb.toString();
    }
}
