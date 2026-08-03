package com.esteirahabitacional.workflow.application.port.out;

import java.time.Instant;
import java.util.UUID;

public interface WorkflowAudit {
    void record(UUID organizationId, UUID processId, UUID actorId, String action, Instant occurredAt);
}
