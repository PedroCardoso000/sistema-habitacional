package com.esteirahabitacional.parties.domain.model;

import java.util.Locale;
import java.util.regex.Pattern;

public record ContactInfo(String email, String phone) {

    private static final Pattern EMAIL = Pattern.compile("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$");

    public ContactInfo {
        email = normalizeEmail(email);
        phone = normalizePhone(phone);
        if (email == null && phone == null) {
            throw new IllegalArgumentException("At least one contact is required");
        }
    }

    private static String normalizeEmail(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        String normalized = value.trim().toLowerCase(Locale.ROOT);
        if (!EMAIL.matcher(normalized).matches()) {
            throw new IllegalArgumentException("A valid contact email is required");
        }
        return normalized;
    }

    private static String normalizePhone(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        String digits = value.replaceAll("\\D", "");
        if (digits.length() < 10 || digits.length() > 13) {
            throw new IllegalArgumentException("A valid contact phone is required");
        }
        return digits;
    }
}
