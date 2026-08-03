package com.esteirahabitacional.parties.application.port.in;

import com.esteirahabitacional.parties.domain.model.PartyStatus;
import java.util.UUID;

public interface RegisterClientUseCase {

    Result execute(Command command);

    record Command(UUID organizationId, String cpf, String fullName, String email, String phone) {}

    record Result(UUID id, String fullName, String email, String phone, PartyStatus status) {}
}
