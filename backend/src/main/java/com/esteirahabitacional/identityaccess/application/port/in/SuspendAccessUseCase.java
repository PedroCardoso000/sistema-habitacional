package com.esteirahabitacional.identityaccess.application.port.in;

import java.util.UUID;

public interface SuspendAccessUseCase {

    void execute(Command command);

    record Command(UUID organizationId, UUID userId) {}
}
