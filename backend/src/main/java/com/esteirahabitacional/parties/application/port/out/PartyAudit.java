package com.esteirahabitacional.parties.application.port.out;

import java.time.Instant;
import java.util.UUID;

public interface PartyAudit {

    void record(Action action);

    record Action(
            UUID id,
            UUID organizationId,
            UUID actorUserId,
            UUID targetId,
            String targetType,
            String action,
            Instant occurredAt) {}
}
