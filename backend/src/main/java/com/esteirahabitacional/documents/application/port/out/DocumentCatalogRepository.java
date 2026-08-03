package com.esteirahabitacional.documents.application.port.out;

import com.esteirahabitacional.documents.domain.model.ChecklistTemplate;
import com.esteirahabitacional.documents.domain.model.DocumentType;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

public interface DocumentCatalogRepository {
    ChecklistTemplate ensureInitial(UUID organizationId, UUID actorId, Instant occurredAt);
    Optional<ChecklistTemplate> findActiveTemplate(UUID organizationId);
    Optional<DocumentType> findType(UUID organizationId, UUID typeId);
}
