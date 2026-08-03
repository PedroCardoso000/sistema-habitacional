package com.esteirahabitacional.parties.domain.model;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

public final class RealEstateAgency {

    private final UUID id;
    private final UUID organizationId;
    private final Cnpj cnpj;
    private final String legalName;
    private final Instant createdAt;
    private ContactInfo contact;
    private PartyStatus status;
    private Instant updatedAt;

    private RealEstateAgency(
            UUID id,
            UUID organizationId,
            Cnpj cnpj,
            String legalName,
            ContactInfo contact,
            PartyStatus status,
            Instant createdAt,
            Instant updatedAt) {
        this.id = Objects.requireNonNull(id, "id is required");
        this.organizationId = Objects.requireNonNull(organizationId, "organizationId is required");
        this.cnpj = Objects.requireNonNull(cnpj, "cnpj is required");
        this.legalName = requireName(legalName);
        this.contact = Objects.requireNonNull(contact, "contact is required");
        this.status = Objects.requireNonNull(status, "status is required");
        this.createdAt = Objects.requireNonNull(createdAt, "createdAt is required");
        this.updatedAt = Objects.requireNonNull(updatedAt, "updatedAt is required");
    }

    public static RealEstateAgency register(
            UUID id, UUID organizationId, Cnpj cnpj, String legalName,
            ContactInfo contact, Instant occurredAt) {
        return new RealEstateAgency(id, organizationId, cnpj, legalName, contact,
                PartyStatus.ACTIVE, occurredAt, occurredAt);
    }

    public static RealEstateAgency restore(
            UUID id, UUID organizationId, Cnpj cnpj, String legalName,
            ContactInfo contact, PartyStatus status, Instant createdAt, Instant updatedAt) {
        return new RealEstateAgency(
                id, organizationId, cnpj, legalName, contact, status, createdAt, updatedAt);
    }

    public void activate(Instant occurredAt) {
        status = PartyStatus.ACTIVE;
        updatedAt = Objects.requireNonNull(occurredAt, "occurredAt is required");
    }

    public void updateContact(ContactInfo newContact, Instant occurredAt) {
        contact = Objects.requireNonNull(newContact, "contact is required");
        updatedAt = Objects.requireNonNull(occurredAt, "occurredAt is required");
    }

    public void deactivate(Instant occurredAt) {
        status = PartyStatus.INACTIVE;
        updatedAt = Objects.requireNonNull(occurredAt, "occurredAt is required");
    }

    public UUID id() { return id; }
    public UUID organizationId() { return organizationId; }
    public Cnpj cnpj() { return cnpj; }
    public String legalName() { return legalName; }
    public ContactInfo contact() { return contact; }
    public PartyStatus status() { return status; }
    public Instant createdAt() { return createdAt; }
    public Instant updatedAt() { return updatedAt; }

    private static String requireName(String name) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Agency legal name is required");
        }
        return name.trim();
    }
}
