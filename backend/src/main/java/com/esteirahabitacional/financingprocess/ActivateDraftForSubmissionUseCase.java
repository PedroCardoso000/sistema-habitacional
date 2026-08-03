package com.esteirahabitacional.financingprocess;

import java.util.UUID;

public interface ActivateDraftForSubmissionUseCase {
    Result activate(Command command);

    record Command(UUID organizationId, UUID processId, long expectedVersion, UUID actorId) {}
    record Result(UUID processId, UUID organizationId, UUID responsibleUserId,
            UUID mainClientId, long version) {}
}
