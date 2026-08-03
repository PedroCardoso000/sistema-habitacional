package com.esteirahabitacional.identityaccess;

import java.util.UUID;

public interface CurrentActorContextUseCase {
    Actor current();

    record Actor(UUID userId, UUID organizationId, Role role) {}
    enum Role { MANAGER, ANALYST, BROKER, CLIENT, SELLER, PLATFORM_ADMIN }
}
