package com.esteirahabitacional.parties.application.port.in;

import com.esteirahabitacional.parties.domain.model.PartyStatus;
import java.util.UUID;

public interface ChangePartnerStatusUseCase {

    void execute(Command command);

    enum PartnerType {
        BROKER,
        AGENCY
    }

    record Command(UUID organizationId, PartnerType type, UUID partnerId, PartyStatus status) {}
}
