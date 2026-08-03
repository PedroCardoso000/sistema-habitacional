package com.esteirahabitacional.identityaccess.application.port.in;

import com.esteirahabitacional.identityaccess.domain.model.Role;
import java.util.UUID;

public interface AssignRoleUseCase {

    void execute(Command command);

    record Command(UUID organizationId, UUID userId, Role role) {}
}
