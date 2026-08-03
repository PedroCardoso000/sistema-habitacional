package com.esteirahabitacional.workflow.domain.model;

import java.time.Instant;
import java.util.UUID;

public record StageTransition(
        int sequence, TransitionType type, String fromStageCode, String toStageCode,
        String justification, UUID actorId, Instant occurredAt) {}
