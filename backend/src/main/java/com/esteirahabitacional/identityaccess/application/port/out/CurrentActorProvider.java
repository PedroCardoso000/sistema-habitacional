package com.esteirahabitacional.identityaccess.application.port.out;

import java.util.UUID;

public interface CurrentActorProvider {

    Actor current();

    record Actor(UUID userId, UUID organizationId) {}
}
