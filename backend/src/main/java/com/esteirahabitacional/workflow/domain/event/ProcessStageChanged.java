package com.esteirahabitacional.workflow.domain.event;

import com.esteirahabitacional.shared.DomainEvent;
import com.esteirahabitacional.workflow.domain.model.TransitionType;
import java.time.Instant;
import java.util.UUID;

public record ProcessStageChanged(
        UUID organizationId, UUID processId, String fromStageCode, String toStageCode,
        TransitionType transitionType, UUID actorId, Instant occurredAt) implements DomainEvent {}
