package com.esteirahabitacional.identityaccess;

import java.util.UUID;

public interface AuthorizePlatformAdministrationUseCase {

    AuthorizedActor requireOrganizationCreationPermission();

    record AuthorizedActor(UUID userId, UUID organizationId) {}
}
