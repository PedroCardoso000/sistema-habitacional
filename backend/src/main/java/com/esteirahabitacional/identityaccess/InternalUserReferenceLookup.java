package com.esteirahabitacional.identityaccess;

import java.util.UUID;

public interface InternalUserReferenceLookup {
    Reference findActive(UUID organizationId, UUID userId);
    record Reference(UUID userId, UUID organizationId, String displayName) {}
}
