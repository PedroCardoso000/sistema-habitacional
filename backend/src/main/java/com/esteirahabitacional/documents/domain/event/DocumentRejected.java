package com.esteirahabitacional.documents.domain.event;

import com.esteirahabitacional.shared.DomainEvent;
import java.time.Instant;
import java.util.UUID;

public record DocumentRejected(UUID organizationId, UUID processId, UUID requestId,
        UUID actorId, Instant occurredAt) implements DomainEvent {}
