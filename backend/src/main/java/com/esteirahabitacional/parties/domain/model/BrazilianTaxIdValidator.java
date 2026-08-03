package com.esteirahabitacional.parties.domain.model;

final class BrazilianTaxIdValidator {

    private BrazilianTaxIdValidator() {}

    static boolean isValidCpf(String digits) {
        if (digits.length() != 11 || hasAllEqualDigits(digits)) {
            return false;
        }
        int first = cpfDigit(digits, 9, 10);
        int second = cpfDigit(digits, 10, 11);
        return first == digitAt(digits, 9) && second == digitAt(digits, 10);
    }

    static boolean isValidCnpj(String digits) {
        if (digits.length() != 14 || hasAllEqualDigits(digits)) {
            return false;
        }
        int first = cnpjDigit(digits, 12);
        int second = cnpjDigit(digits, 13);
        return first == digitAt(digits, 12) && second == digitAt(digits, 13);
    }

    private static int cpfDigit(String digits, int length, int weight) {
        int sum = 0;
        for (int index = 0; index < length; index++) {
            sum += digitAt(digits, index) * (weight - index);
        }
        int remainder = (sum * 10) % 11;
        return remainder == 10 ? 0 : remainder;
    }

    private static int cnpjDigit(String digits, int length) {
        int[] weights = length == 12
                ? new int[] {5, 4, 3, 2, 9, 8, 7, 6, 5, 4, 3, 2}
                : new int[] {6, 5, 4, 3, 2, 9, 8, 7, 6, 5, 4, 3, 2};
        int sum = 0;
        for (int index = 0; index < length; index++) {
            sum += digitAt(digits, index) * weights[index];
        }
        int remainder = sum % 11;
        return remainder < 2 ? 0 : 11 - remainder;
    }

    private static boolean hasAllEqualDigits(String digits) {
        return digits.chars().allMatch(value -> value == digits.charAt(0));
    }

    private static int digitAt(String digits, int index) {
        return digits.charAt(index) - '0';
    }
}
