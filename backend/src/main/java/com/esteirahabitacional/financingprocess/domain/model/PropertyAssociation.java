package com.esteirahabitacional.financingprocess.domain.model;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

public record PropertyAssociation(
        int sequence, String addressLine, String city, String state, String postalCode,
        UUID associatedBy, Instant associatedAt) {
    public PropertyAssociation {
        if (sequence < 1) {
            throw new IllegalArgumentException("sequence must be positive");
        }
        addressLine = required(addressLine, "addressLine");
        city = required(city, "city");
        state = required(state, "state");
        postalCode = required(postalCode, "postalCode");
        Objects.requireNonNull(associatedBy, "associatedBy is required");
        Objects.requireNonNull(associatedAt, "associatedAt is required");
    }

    private static String required(String value, String field) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(field + " is required");
        }
        return value.trim();
    }
}
