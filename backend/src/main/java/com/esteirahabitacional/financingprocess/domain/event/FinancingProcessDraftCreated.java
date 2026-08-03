package com.esteirahabitacional.financingprocess.domain.event;

import com.esteirahabitacional.financingprocess.domain.model.ProcessOrigin;
import com.esteirahabitacional.shared.DomainEvent;
import java.time.Instant;
import java.util.UUID;

public record FinancingProcessDraftCreated(
        UUID processId, UUID organizationId, String processNumber, ProcessOrigin origin,
        UUID actorId, Instant occurredAt) implements DomainEvent {}
