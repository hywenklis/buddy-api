package com.buddy.api.utils;

import java.util.Random;

public class RandomCpfUtils {

    private static final Random RANDOM = new Random();

    public static String generateValidCpf() {
        int[] cpf = new int[11];
        for (int i = 0; i < 9; i++) {
            cpf[i] = RANDOM.nextInt(10);
        }

        cpf[9] = calculateCpfDigit(cpf, 9);
        cpf[10] = calculateCpfDigit(cpf, 10);

        StringBuilder sb = new StringBuilder();
        for (int digit : cpf) {
            sb.append(digit);
        }

        return sb.toString();
    }

    private static int calculateCpfDigit(int[] cpf, int position) {
        int sum = 0;
        for (int i = 0; i < position; i++) {
            sum += cpf[i] * (position + 1 - i);
        }
        int digit = 11 - (sum % 11);
        return digit > 9 ? 0 : digit;
    }
}
