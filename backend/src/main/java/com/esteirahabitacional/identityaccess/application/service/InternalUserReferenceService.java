package com.esteirahabitacional.identityaccess.application.service;

import com.esteirahabitacional.identityaccess.InternalUserReferenceLookup;
import com.esteirahabitacional.identityaccess.application.port.out.UserRepository;
import com.esteirahabitacional.identityaccess.domain.model.AccessStatus;
import com.esteirahabitacional.identityaccess.domain.model.User;
import com.esteirahabitacional.shared.ApplicationException;
import java.util.UUID;

public final class InternalUserReferenceService implements InternalUserReferenceLookup {
    private final UserRepository users;
    public InternalUserReferenceService(UserRepository users) { this.users = users; }

    @Override
    public Reference findActive(UUID organizationId, UUID userId) {
        User user = users.findById(organizationId, userId).orElseThrow(() -> notFound());
        if (user.status() != AccessStatus.ACTIVE || !user.role().isInternal()) {
            throw new ApplicationException(422, "invalid-internal-user", "Responsável inválido",
                    "O responsável deve ser um usuário interno ativo da empresa.");
        }
        return new Reference(user.id(), user.organizationId(), user.displayName());
    }

    private ApplicationException notFound() {
        return new ApplicationException(404, "internal-user-not-found", "Usuário não encontrado",
                "O usuário interno não existe nesta empresa.");
    }
}
