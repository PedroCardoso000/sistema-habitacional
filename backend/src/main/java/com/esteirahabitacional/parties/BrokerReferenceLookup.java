package com.esteirahabitacional.parties;

import java.util.UUID;

public interface BrokerReferenceLookup {

    Reference findActive(UUID organizationId, UUID brokerId);

    record Reference(UUID id, UUID organizationId, String fullName, UUID realEstateAgencyId) {}
}
