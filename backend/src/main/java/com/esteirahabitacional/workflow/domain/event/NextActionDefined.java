package com.esteirahabitacional.workflow.domain.event;

import com.esteirahabitacional.shared.DomainEvent;
import java.time.Instant;
import java.util.UUID;

public record NextActionDefined(
        UUID organizationId, UUID processId, UUID responsibleUserId,
        Instant dueAt, UUID actorId, Instant occurredAt) implements DomainEvent {}
