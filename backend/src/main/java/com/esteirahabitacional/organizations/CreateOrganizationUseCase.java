package com.esteirahabitacional.organizations;

import java.util.UUID;

public interface CreateOrganizationUseCase {

    Result execute(Command command);

    record Command(String name) {}

    record Result(UUID id, String name) {}
}
