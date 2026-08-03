package com.esteirahabitacional.workflow.domain.model;

import java.util.Objects;
import java.util.Set;

public record WorkflowStageDefinition(
        String code, String name, int position, Set<String> requiredExitCriteria) {
    public WorkflowStageDefinition {
        code = required(code, "code");
        name = required(name, "name");
        if (position < 1) {
            throw new IllegalArgumentException("position must be positive");
        }
        requiredExitCriteria = Set.copyOf(Objects.requireNonNull(
                requiredExitCriteria, "requiredExitCriteria is required"));
    }

    public boolean canExitWith(Set<String> satisfiedCriteria) {
        return satisfiedCriteria.containsAll(requiredExitCriteria);
    }

    private static String required(String value, String field) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(field + " is required");
        }
        return value.trim();
    }
}
