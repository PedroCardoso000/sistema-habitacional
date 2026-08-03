package com.esteirahabitacional.workflow.domain.model;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

public record NextAction(String description, UUID responsibleUserId, Instant dueAt, Instant definedAt) {
    public NextAction {
        if (description == null || description.isBlank()) {
            throw new IllegalArgumentException("Next action description is required");
        }
        description = description.trim();
        Objects.requireNonNull(responsibleUserId, "Next action responsible is required");
        Objects.requireNonNull(definedAt, "definedAt is required");
    }
}
