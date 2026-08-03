package com.esteirahabitacional.identityaccess;

import java.util.UUID;

public interface AuthorizeOrganizationUseCase {

    AuthorizedActor require(UUID organizationId, Action action);

    enum Action {
        MANAGE_PARTIES,
        VIEW_PARTIES,
        MANAGE_PROCESSES,
        VIEW_PROCESSES,
        AUTHORIZE_WORKFLOW_EXCEPTION
    }

    record AuthorizedActor(UUID userId, UUID organizationId) {}
}
