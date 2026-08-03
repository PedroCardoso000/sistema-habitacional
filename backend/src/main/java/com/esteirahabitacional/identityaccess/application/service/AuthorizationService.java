package com.esteirahabitacional.identityaccess.application.service;

import com.esteirahabitacional.identityaccess.AuthorizePlatformAdministrationUseCase;
import com.esteirahabitacional.identityaccess.AuthorizeOrganizationUseCase;
import com.esteirahabitacional.identityaccess.application.port.out.CurrentActorProvider;
import com.esteirahabitacional.identityaccess.application.port.out.UserRepository;
import com.esteirahabitacional.identityaccess.domain.model.Permission;
import com.esteirahabitacional.identityaccess.domain.model.User;
import java.util.UUID;

public class AuthorizationService implements AuthorizePlatformAdministrationUseCase, AuthorizeOrganizationUseCase {

    private final CurrentActorProvider actors;
    private final UserRepository users;

    public AuthorizationService(CurrentActorProvider actors, UserRepository users) {
        this.actors = actors;
        this.users = users;
    }

    public User require(UUID organizationId, Permission permission) {
        CurrentActorProvider.Actor actor = actors.current();
        if (!actor.organizationId().equals(organizationId)) {
            throw IdentityAccessExceptions.forbidden();
        }
        User user = users.findById(organizationId, actor.userId())
                .orElseThrow(IdentityAccessExceptions::forbidden);
        if (!user.can(permission)) {
            throw IdentityAccessExceptions.forbidden();
        }
        return user;
    }

    @Override
    public AuthorizePlatformAdministrationUseCase.AuthorizedActor requireOrganizationCreationPermission() {
        CurrentActorProvider.Actor actor = actors.current();
        require(actor.organizationId(), Permission.CREATE_ORGANIZATION);
        return new AuthorizePlatformAdministrationUseCase.AuthorizedActor(
                actor.userId(), actor.organizationId());
    }

    @Override
    public AuthorizeOrganizationUseCase.AuthorizedActor require(UUID organizationId, Action action) {
        User user = require(organizationId, Permission.valueOf(action.name()));
        return new AuthorizeOrganizationUseCase.AuthorizedActor(user.id(), user.organizationId());
    }
}
