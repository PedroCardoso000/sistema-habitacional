package com.esteirahabitacional.parties;

import java.util.UUID;

public interface ClientReferenceLookup {

    Reference find(UUID organizationId, UUID clientId);

    record Reference(UUID id, UUID organizationId, String fullName) {}
}
