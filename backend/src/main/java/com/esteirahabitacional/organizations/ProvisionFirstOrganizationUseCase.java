package com.esteirahabitacional.organizations;

import java.util.UUID;

public interface ProvisionFirstOrganizationUseCase {

    Result execute(Command command);

    record Command(String name) {}

    record Result(UUID id, String name) {}
}
