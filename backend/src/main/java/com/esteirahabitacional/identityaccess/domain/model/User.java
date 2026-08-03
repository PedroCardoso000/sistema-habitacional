package com.esteirahabitacional.identityaccess.domain.model;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

public final class User {

    private final UUID id;
    private final UUID organizationId;
    private final Email email;
    private final String displayName;
    private final Instant createdAt;
    private Role role;
    private AccessStatus status;
    private Instant accessChangedAt;

    private User(
            UUID id,
            UUID organizationId,
            Email email,
            String displayName,
            Role role,
            AccessStatus status,
            Instant createdAt,
            Instant accessChangedAt) {
        this.id = Objects.requireNonNull(id, "id is required");
        this.organizationId = Objects.requireNonNull(organizationId, "organizationId is required");
        this.email = Objects.requireNonNull(email, "email is required");
        this.displayName = requireDisplayName(displayName);
        this.role = Objects.requireNonNull(role, "role is required");
        this.status = Objects.requireNonNull(status, "status is required");
        this.createdAt = Objects.requireNonNull(createdAt, "createdAt is required");
        this.accessChangedAt = Objects.requireNonNull(accessChangedAt, "accessChangedAt is required");
    }

    public static User registerInternal(
            UUID id, UUID organizationId, Email email, String displayName, Role role, Instant occurredAt) {
        requireInternalRole(role);
        return new User(
                id, organizationId, email, displayName, role, AccessStatus.ACTIVE, occurredAt, occurredAt);
    }

    public static User provisionPlatformAdministrator(
            UUID id, UUID organizationId, Email email, String displayName, Instant occurredAt) {
        return new User(id, organizationId, email, displayName, Role.PLATFORM_ADMIN,
                AccessStatus.ACTIVE, occurredAt, occurredAt);
    }

    public static User restore(
            UUID id,
            UUID organizationId,
            Email email,
            String displayName,
            Role role,
            AccessStatus status,
            Instant createdAt,
            Instant accessChangedAt) {
        return new User(id, organizationId, email, displayName, role, status, createdAt, accessChangedAt);
    }

    public void assignInternalRole(Role newRole, Instant occurredAt) {
        ensureNotRevoked();
        requireInternalRole(newRole);
        role = newRole;
        accessChangedAt = Objects.requireNonNull(occurredAt, "occurredAt is required");
    }

    public void suspend(Instant occurredAt) {
        ensureNotRevoked();
        status = AccessStatus.SUSPENDED;
        accessChangedAt = Objects.requireNonNull(occurredAt, "occurredAt is required");
    }

    public void revoke(Instant occurredAt) {
        ensureNotRevoked();
        status = AccessStatus.REVOKED;
        accessChangedAt = Objects.requireNonNull(occurredAt, "occurredAt is required");
    }

    public boolean can(Permission permission) {
        return status == AccessStatus.ACTIVE && role.grants(permission);
    }

    public UUID id() { return id; }
    public UUID organizationId() { return organizationId; }
    public Email email() { return email; }
    public String displayName() { return displayName; }
    public Role role() { return role; }
    public AccessStatus status() { return status; }
    public Instant createdAt() { return createdAt; }
    public Instant accessChangedAt() { return accessChangedAt; }

    private void ensureNotRevoked() {
        if (status == AccessStatus.REVOKED) {
            throw new IllegalStateException("Revoked access cannot be changed");
        }
    }

    private static void requireInternalRole(Role role) {
        if (role == null || !role.isInternal()) {
            throw new IllegalArgumentException("An internal role is required");
        }
    }

    private static String requireDisplayName(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("Display name is required");
        }
        return value.trim();
    }
}
