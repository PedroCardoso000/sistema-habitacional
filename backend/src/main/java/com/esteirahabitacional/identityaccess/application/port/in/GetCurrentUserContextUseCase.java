package com.esteirahabitacional.identityaccess.application.port.in;

import com.esteirahabitacional.identityaccess.domain.model.AccessStatus;
import com.esteirahabitacional.identityaccess.domain.model.Role;
import java.util.UUID;

public interface GetCurrentUserContextUseCase {

    Result execute();

    record Result(UUID userId, UUID organizationId, String email, String displayName,
                  Role role, AccessStatus status) {}
}
