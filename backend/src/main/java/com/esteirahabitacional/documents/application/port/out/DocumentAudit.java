package com.esteirahabitacional.documents.application.port.out;

import java.time.Instant;
import java.util.UUID;

public interface DocumentAudit {
    void record(UUID organizationId, UUID processId, UUID actorId, String action, Instant occurredAt);
}
