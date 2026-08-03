package com.esteirahabitacional.identityaccess.application.service;

import com.esteirahabitacional.identityaccess.application.port.in.GetCurrentUserContextUseCase;
import com.esteirahabitacional.identityaccess.application.port.out.CurrentActorProvider;
import com.esteirahabitacional.identityaccess.application.port.out.UserRepository;
import com.esteirahabitacional.identityaccess.domain.model.Permission;
import com.esteirahabitacional.identityaccess.domain.model.User;

public class GetCurrentUserContextService implements GetCurrentUserContextUseCase {

    private final CurrentActorProvider actors;
    private final UserRepository users;

    public GetCurrentUserContextService(CurrentActorProvider actors, UserRepository users) {
        this.actors = actors;
        this.users = users;
    }

    @Override
    public Result execute() {
        CurrentActorProvider.Actor actor = actors.current();
        User user = users.findById(actor.organizationId(), actor.userId())
                .orElseThrow(IdentityAccessExceptions::forbidden);
        if (!user.can(Permission.VIEW_CURRENT_CONTEXT)) {
            throw IdentityAccessExceptions.forbidden();
        }
        return new Result(
                user.id(), user.organizationId(), user.email().value(), user.displayName(),
                user.role(), user.status());
    }
}
