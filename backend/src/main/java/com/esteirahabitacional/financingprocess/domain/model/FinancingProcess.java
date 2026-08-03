package com.esteirahabitacional.financingprocess.domain.model;

import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

public final class FinancingProcess {
    private final UUID id;
    private final String processNumber;
    private final UUID organizationId;
    private final ProcessOrigin origin;
    private final UUID authorUserId;
    private final UUID brokerId;
    private final UUID responsibleUserId;
    private final Instant createdAt;
    private UUID mainClientId;
    private ProcessPriority priority;
    private final Set<ProcessParticipant> participants;
    private final List<PropertyAssociation> propertyHistory;
    private long version;
    private Instant updatedAt;

    private FinancingProcess(UUID id, String processNumber, UUID organizationId, ProcessOrigin origin,
            UUID authorUserId, UUID brokerId, UUID responsibleUserId, UUID mainClientId,
            ProcessPriority priority, Set<ProcessParticipant> participants,
            List<PropertyAssociation> propertyHistory, long version, Instant createdAt, Instant updatedAt) {
        this.id = Objects.requireNonNull(id, "id is required");
        this.processNumber = required(processNumber, "processNumber");
        this.organizationId = Objects.requireNonNull(organizationId, "organizationId is required");
        this.origin = Objects.requireNonNull(origin, "origin is required");
        this.authorUserId = Objects.requireNonNull(authorUserId, "authorUserId is required");
        this.responsibleUserId = Objects.requireNonNull(responsibleUserId, "responsibleUserId is required");
        if (origin == ProcessOrigin.BROKER && brokerId == null) {
            throw new IllegalArgumentException("brokerId is required for broker origin");
        }
        if (origin == ProcessOrigin.DIRECT_CLIENT && brokerId != null) {
            throw new IllegalArgumentException("brokerId is not allowed for direct client origin");
        }
        this.brokerId = brokerId;
        this.mainClientId = mainClientId;
        this.priority = Objects.requireNonNull(priority, "priority is required");
        this.participants = new LinkedHashSet<>(participants);
        this.propertyHistory = new ArrayList<>(propertyHistory);
        this.version = version;
        this.createdAt = Objects.requireNonNull(createdAt, "createdAt is required");
        this.updatedAt = Objects.requireNonNull(updatedAt, "updatedAt is required");
    }

    public static FinancingProcess draft(UUID id, String number, UUID organizationId, ProcessOrigin origin,
            UUID authorUserId, UUID brokerId, UUID mainClientId, Instant occurredAt) {
        Set<ProcessParticipant> initial = new LinkedHashSet<>();
        if (brokerId != null) {
            initial.add(new ProcessParticipant(ParticipantType.BROKER, brokerId));
        }
        if (mainClientId != null) {
            initial.add(new ProcessParticipant(ParticipantType.CLIENT, mainClientId));
        }
        return new FinancingProcess(id, number, organizationId, origin, authorUserId, brokerId,
                authorUserId, mainClientId, ProcessPriority.NORMAL, initial, List.of(), 0, occurredAt, occurredAt);
    }

    public static FinancingProcess restore(UUID id, String number, UUID organizationId, ProcessOrigin origin,
            UUID authorUserId, UUID brokerId, UUID responsibleUserId, UUID mainClientId,
            ProcessPriority priority, Set<ProcessParticipant> participants,
            List<PropertyAssociation> propertyHistory, long version, Instant createdAt, Instant updatedAt) {
        return new FinancingProcess(id, number, organizationId, origin, authorUserId, brokerId,
                responsibleUserId, mainClientId, priority, participants, propertyHistory, version, createdAt, updatedAt);
    }

    public void defineMainClient(UUID clientId, Instant occurredAt) {
        mainClientId = Objects.requireNonNull(clientId, "clientId is required");
        participants.add(new ProcessParticipant(ParticipantType.CLIENT, clientId));
        touch(occurredAt);
    }

    public void associateParticipant(ProcessParticipant participant, Instant occurredAt) {
        participants.add(Objects.requireNonNull(participant, "participant is required"));
        touch(occurredAt);
    }

    public void associateProperty(String addressLine, String city, String state, String postalCode,
            UUID actorId, Instant occurredAt) {
        propertyHistory.add(new PropertyAssociation(propertyHistory.size() + 1, addressLine, city,
                state, postalCode, actorId, occurredAt));
        touch(occurredAt);
    }

    public void changePriority(ProcessPriority newPriority, Instant occurredAt) {
        priority = Objects.requireNonNull(newPriority, "priority is required");
        touch(occurredAt);
    }

    public boolean isLinkedBroker(UUID candidateBrokerId) {
        return brokerId != null && brokerId.equals(candidateBrokerId);
    }

    public UUID id() { return id; }
    public String processNumber() { return processNumber; }
    public UUID organizationId() { return organizationId; }
    public ProcessOrigin origin() { return origin; }
    public ProcessStatus status() { return ProcessStatus.DRAFT; }
    public UUID authorUserId() { return authorUserId; }
    public UUID brokerId() { return brokerId; }
    public UUID responsibleUserId() { return responsibleUserId; }
    public UUID mainClientId() { return mainClientId; }
    public ProcessPriority priority() { return priority; }
    public Set<ProcessParticipant> participants() { return Set.copyOf(participants); }
    public List<PropertyAssociation> propertyHistory() { return List.copyOf(propertyHistory); }
    public long version() { return version; }
    public Instant createdAt() { return createdAt; }
    public Instant updatedAt() { return updatedAt; }
    public void persistedAtVersion(long persistedVersion) { version = persistedVersion; }

    private void touch(Instant occurredAt) { updatedAt = Objects.requireNonNull(occurredAt, "occurredAt is required"); }
    private static String required(String value, String field) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(field + " is required");
        }
        return value.trim();
    }
}
