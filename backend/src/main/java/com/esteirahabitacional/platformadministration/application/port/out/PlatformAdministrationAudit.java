package com.esteirahabitacional.platformadministration.application.port.out;

import java.time.Instant;
import java.util.UUID;

public interface PlatformAdministrationAudit {

    void recordOrganizationCreated(
            UUID actionId,
            UUID actorOrganizationId,
            UUID actorUserId,
            UUID createdOrganizationId,
            Instant occurredAt);
}
