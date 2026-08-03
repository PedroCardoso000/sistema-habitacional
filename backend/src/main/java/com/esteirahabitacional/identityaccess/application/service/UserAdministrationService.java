package com.esteirahabitacional.identityaccess.application.service;

import com.esteirahabitacional.identityaccess.application.port.in.AssignRoleUseCase;
import com.esteirahabitacional.identityaccess.application.port.in.RegisterInternalUserUseCase;
import com.esteirahabitacional.identityaccess.application.port.in.RevokeAccessUseCase;
import com.esteirahabitacional.identityaccess.application.port.in.SuspendAccessUseCase;
import com.esteirahabitacional.identityaccess.application.port.out.UserRepository;
import com.esteirahabitacional.identityaccess.application.port.out.UserRepository.AccessAction;
import com.esteirahabitacional.identityaccess.domain.model.Email;
import com.esteirahabitacional.identityaccess.domain.model.Permission;
import com.esteirahabitacional.identityaccess.domain.model.User;
import com.esteirahabitacional.shared.CurrentTimeProvider;
import com.esteirahabitacional.shared.IdentifierGenerator;
import java.time.Instant;
import java.util.UUID;

public class UserAdministrationService implements RegisterInternalUserUseCase, AssignRoleUseCase,
        SuspendAccessUseCase, RevokeAccessUseCase {

    private final AuthorizationService authorization;
    private final UserRepository users;
    private final IdentifierGenerator identifiers;
    private final CurrentTimeProvider time;

    public UserAdministrationService(
            AuthorizationService authorization,
            UserRepository users,
            IdentifierGenerator identifiers,
            CurrentTimeProvider time) {
        this.authorization = authorization;
        this.users = users;
        this.identifiers = identifiers;
        this.time = time;
    }

    @Override
    public RegisterInternalUserUseCase.Result execute(RegisterInternalUserUseCase.Command command) {
        User actor = authorization.require(command.organizationId(), Permission.MANAGE_INTERNAL_USERS);
        Email email = new Email(command.email());
        if (users.existsByEmail(command.organizationId(), email)) {
            throw IdentityAccessExceptions.duplicateEmail();
        }
        Instant occurredAt = time.now();
        User user;
        try {
            user = User.registerInternal(
                    identifiers.generate(), command.organizationId(), email,
                    command.displayName(), command.role(), occurredAt);
        } catch (IllegalArgumentException exception) {
            throw IdentityAccessExceptions.invalidOperation(exception.getMessage());
        }
        users.save(user);
        audit(user.organizationId(), actor.id(), user.id(), "INTERNAL_USER_REGISTERED", occurredAt);
        return toResult(user);
    }

    @Override
    public void execute(AssignRoleUseCase.Command command) {
        User actor = authorization.require(command.organizationId(), Permission.MANAGE_INTERNAL_USERS);
        User target = findTarget(command.organizationId(), command.userId());
        Instant occurredAt = time.now();
        try {
            target.assignInternalRole(command.role(), occurredAt);
        } catch (IllegalArgumentException | IllegalStateException exception) {
            throw IdentityAccessExceptions.invalidOperation(exception.getMessage());
        }
        users.save(target);
        audit(command.organizationId(), actor.id(), target.id(), "ROLE_ASSIGNED", occurredAt);
    }

    @Override
    public void execute(SuspendAccessUseCase.Command command) {
        User actor = authorization.require(command.organizationId(), Permission.MANAGE_INTERNAL_USERS);
        User target = findTarget(command.organizationId(), command.userId());
        Instant occurredAt = time.now();
        try {
            target.suspend(occurredAt);
        } catch (IllegalStateException exception) {
            throw IdentityAccessExceptions.invalidOperation(exception.getMessage());
        }
        users.save(target);
        audit(command.organizationId(), actor.id(), target.id(), "ACCESS_SUSPENDED", occurredAt);
    }

    @Override
    public void execute(RevokeAccessUseCase.Command command) {
        User actor = authorization.require(command.organizationId(), Permission.MANAGE_INTERNAL_USERS);
        User target = findTarget(command.organizationId(), command.userId());
        Instant occurredAt = time.now();
        try {
            target.revoke(occurredAt);
        } catch (IllegalStateException exception) {
            throw IdentityAccessExceptions.invalidOperation(exception.getMessage());
        }
        users.save(target);
        audit(command.organizationId(), actor.id(), target.id(), "ACCESS_REVOKED", occurredAt);
    }

    private User findTarget(UUID organizationId, UUID userId) {
        return users.findById(organizationId, userId)
                .orElseThrow(IdentityAccessExceptions::userNotFound);
    }

    private void audit(UUID organizationId, UUID actorId, UUID targetId, String action, Instant occurredAt) {
        users.recordAccessAction(new AccessAction(
                identifiers.generate(), organizationId, actorId, targetId, action, occurredAt));
    }

    private RegisterInternalUserUseCase.Result toResult(User user) {
        return new RegisterInternalUserUseCase.Result(
                user.id(), user.organizationId(), user.email().value(), user.displayName(),
                user.role(), user.status());
    }
}
