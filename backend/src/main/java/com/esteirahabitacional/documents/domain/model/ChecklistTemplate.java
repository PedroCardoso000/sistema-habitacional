package com.esteirahabitacional.documents.domain.model;

import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public record ChecklistTemplate(UUID id, UUID organizationId, int version,
        String name, List<Item> items, Instant createdAt) {
    public ChecklistTemplate {
        Objects.requireNonNull(id, "id is required");
        Objects.requireNonNull(organizationId, "organizationId is required");
        if (version < 1 || name == null || name.isBlank() || items == null || items.isEmpty()) {
            throw new IllegalArgumentException("Valid checklist template is required");
        }
        items = List.copyOf(items);
        Objects.requireNonNull(createdAt, "createdAt is required");
    }

    public record Item(UUID documentTypeId, boolean required) {
        public Item { Objects.requireNonNull(documentTypeId, "documentTypeId is required"); }
    }
}
