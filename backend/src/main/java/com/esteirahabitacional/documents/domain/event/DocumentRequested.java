package com.esteirahabitacional.documents.domain.event;

import com.esteirahabitacional.shared.DomainEvent;
import java.time.Instant;
import java.util.UUID;

public record DocumentRequested(UUID organizationId, UUID processId, UUID requestId,
        UUID recipientId, UUID actorId, Instant occurredAt) implements DomainEvent {}
