package com.esteirahabitacional.workflow.domain.event;

import com.esteirahabitacional.shared.DomainEvent;
import java.time.Instant;
import java.util.UUID;

public record WorkflowInitialized(
        UUID organizationId, UUID processId, UUID journeyId, int workflowVersion,
        UUID actorId, Instant occurredAt) implements DomainEvent {}
