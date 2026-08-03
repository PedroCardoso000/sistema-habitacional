package com.esteirahabitacional.parties.domain.model;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

public final class Broker {

    private final UUID id;
    private final UUID organizationId;
    private final Cpf cpf;
    private final String fullName;
    private final Instant createdAt;
    private ContactInfo contact;
    private UUID realEstateAgencyId;
    private PartyStatus status;
    private Instant updatedAt;

    private Broker(
            UUID id,
            UUID organizationId,
            Cpf cpf,
            String fullName,
            ContactInfo contact,
            UUID realEstateAgencyId,
            PartyStatus status,
            Instant createdAt,
            Instant updatedAt) {
        this.id = Objects.requireNonNull(id, "id is required");
        this.organizationId = Objects.requireNonNull(organizationId, "organizationId is required");
        this.cpf = Objects.requireNonNull(cpf, "cpf is required");
        this.fullName = requireName(fullName);
        this.contact = Objects.requireNonNull(contact, "contact is required");
        this.realEstateAgencyId = realEstateAgencyId;
        this.status = Objects.requireNonNull(status, "status is required");
        this.createdAt = Objects.requireNonNull(createdAt, "createdAt is required");
        this.updatedAt = Objects.requireNonNull(updatedAt, "updatedAt is required");
    }

    public static Broker register(
            UUID id, UUID organizationId, Cpf cpf, String fullName, ContactInfo contact, Instant occurredAt) {
        return new Broker(id, organizationId, cpf, fullName, contact, null,
                PartyStatus.ACTIVE, occurredAt, occurredAt);
    }

    public static Broker restore(
            UUID id,
            UUID organizationId,
            Cpf cpf,
            String fullName,
            ContactInfo contact,
            UUID realEstateAgencyId,
            PartyStatus status,
            Instant createdAt,
            Instant updatedAt) {
        return new Broker(
                id, organizationId, cpf, fullName, contact, realEstateAgencyId, status, createdAt, updatedAt);
    }

    public void associateWithAgency(UUID agencyId, Instant occurredAt) {
        realEstateAgencyId = Objects.requireNonNull(agencyId, "agencyId is required");
        updatedAt = Objects.requireNonNull(occurredAt, "occurredAt is required");
    }

    public void updateContact(ContactInfo newContact, Instant occurredAt) {
        contact = Objects.requireNonNull(newContact, "contact is required");
        updatedAt = Objects.requireNonNull(occurredAt, "occurredAt is required");
    }

    public void activate(Instant occurredAt) {
        status = PartyStatus.ACTIVE;
        updatedAt = Objects.requireNonNull(occurredAt, "occurredAt is required");
    }

    public void deactivate(Instant occurredAt) {
        status = PartyStatus.INACTIVE;
        updatedAt = Objects.requireNonNull(occurredAt, "occurredAt is required");
    }

    public void ensureCanOriginateProcess() {
        if (status != PartyStatus.ACTIVE) {
            throw new IllegalStateException("Inactive broker cannot originate a financing process");
        }
    }

    public UUID id() { return id; }
    public UUID organizationId() { return organizationId; }
    public Cpf cpf() { return cpf; }
    public String fullName() { return fullName; }
    public ContactInfo contact() { return contact; }
    public UUID realEstateAgencyId() { return realEstateAgencyId; }
    public PartyStatus status() { return status; }
    public Instant createdAt() { return createdAt; }
    public Instant updatedAt() { return updatedAt; }

    private static String requireName(String name) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Broker name is required");
        }
        return name.trim();
    }
}
