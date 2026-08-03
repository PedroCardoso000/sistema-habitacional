package com.esteirahabitacional.platformadministration.application.port.in;

import java.util.UUID;

public interface BootstrapFirstOrganizationUseCase {

    Result execute(Command command);

    record Command(String secret, String organizationName, String administratorEmail,
                   String administratorDisplayName) {}

    record Result(UUID organizationId, UUID administratorUserId) {}
}
