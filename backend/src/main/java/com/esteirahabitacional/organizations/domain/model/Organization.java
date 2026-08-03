package com.esteirahabitacional.organizations.domain.model;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

public final class Organization {

    private final UUID id;
    private final String name;
    private final Instant createdAt;

    private Organization(UUID id, String name, Instant createdAt) {
        this.id = Objects.requireNonNull(id, "id is required");
        this.name = requireName(name);
        this.createdAt = Objects.requireNonNull(createdAt, "createdAt is required");
    }

    public static Organization create(UUID id, String name, Instant createdAt) {
        return new Organization(id, name, createdAt);
    }

    public UUID id() {
        return id;
    }

    public String name() {
        return name;
    }

    public Instant createdAt() {
        return createdAt;
    }

    private static String requireName(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("Organization name is required");
        }
        return value.trim();
    }
}
