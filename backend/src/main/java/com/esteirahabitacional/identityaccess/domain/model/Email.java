package com.esteirahabitacional.identityaccess.domain.model;

import java.util.Locale;
import java.util.regex.Pattern;

public record Email(String value) {

    private static final Pattern FORMAT = Pattern.compile("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$");

    public Email {
        if (value == null || !FORMAT.matcher(value.trim()).matches()) {
            throw new IllegalArgumentException("A valid email is required");
        }
        value = value.trim().toLowerCase(Locale.ROOT);
    }
}
