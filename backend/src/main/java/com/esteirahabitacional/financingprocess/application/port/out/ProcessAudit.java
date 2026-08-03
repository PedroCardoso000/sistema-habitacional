package com.esteirahabitacional.financingprocess.application.port.out;

import java.time.Instant;
import java.util.UUID;

public interface ProcessAudit {
    void record(UUID organizationId, UUID processId, UUID actorId, String action, Instant occurredAt);
}
