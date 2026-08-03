package com.esteirahabitacional.parties.application.port.in;

import java.util.UUID;

public interface UpdatePartyContactUseCase {

    void execute(Command command);

    enum PartyType {
        CLIENT,
        BROKER,
        AGENCY
    }

    record Command(UUID organizationId, PartyType type, UUID partyId, String email, String phone) {}
}
