package com.esteirahabitacional.platformadministration.application.port.in;

import java.util.UUID;

public interface CreateAuthorizedOrganizationUseCase {

    Result execute(Command command);

    record Command(String name) {}

    record Result(UUID id, String name) {}
}
