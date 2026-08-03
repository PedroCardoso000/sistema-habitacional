package com.esteirahabitacional.parties.application.port.in;

import com.esteirahabitacional.parties.domain.model.PartyStatus;
import java.util.UUID;

public interface FindClientByCpfUseCase {

    Result execute(Query query);

    record Query(UUID organizationId, String cpf) {}

    record Result(UUID id, String fullName, String email, String phone, PartyStatus status) {}
}
