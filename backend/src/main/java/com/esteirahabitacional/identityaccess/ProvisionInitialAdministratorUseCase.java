package com.esteirahabitacional.identityaccess;

import java.util.UUID;

public interface ProvisionInitialAdministratorUseCase {

    Result execute(Command command);

    record Command(UUID organizationId, String email, String displayName) {}

    record Result(UUID userId) {}
}
