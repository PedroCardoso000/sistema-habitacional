package com.esteirahabitacional.parties.application.port.in;

import com.esteirahabitacional.parties.domain.model.PartyStatus;
import java.util.UUID;

public interface RegisterAgencyUseCase {

    Result execute(Command command);

    record Command(UUID organizationId, String cnpj, String legalName, String email, String phone) {}

    record Result(UUID id, String legalName, String email, String phone, PartyStatus status) {}
}
