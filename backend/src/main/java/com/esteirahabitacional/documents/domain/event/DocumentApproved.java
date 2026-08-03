package com.esteirahabitacional.documents.domain.event;

import com.esteirahabitacional.shared.DomainEvent;
import java.time.Instant;
import java.util.UUID;

public record DocumentApproved(UUID organizationId, UUID processId, UUID requestId,
        UUID actorId, Instant occurredAt) implements DomainEvent {}
