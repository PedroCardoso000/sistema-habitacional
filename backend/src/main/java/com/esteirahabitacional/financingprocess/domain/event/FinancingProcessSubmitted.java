package com.esteirahabitacional.financingprocess.domain.event;

import com.esteirahabitacional.shared.DomainEvent;
import java.time.Instant;
import java.util.UUID;

public record FinancingProcessSubmitted(UUID processId, UUID organizationId,
        UUID actorId, Instant occurredAt) implements DomainEvent {}
