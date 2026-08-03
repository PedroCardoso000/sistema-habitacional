package com.esteirahabitacional.parties.application.port.in;

import java.util.UUID;

public interface AssociateBrokerAgencyUseCase {

    void execute(Command command);

    record Command(UUID organizationId, UUID brokerId, UUID agencyId) {}
}
