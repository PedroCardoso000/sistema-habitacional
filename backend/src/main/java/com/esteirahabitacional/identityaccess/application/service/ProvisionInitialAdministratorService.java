package com.esteirahabitacional.identityaccess.application.service;

import com.esteirahabitacional.identityaccess.ProvisionInitialAdministratorUseCase;
import com.esteirahabitacional.identityaccess.application.port.out.UserRepository;
import com.esteirahabitacional.identityaccess.application.port.out.UserRepository.AccessAction;
import com.esteirahabitacional.identityaccess.domain.model.Email;
import com.esteirahabitacional.identityaccess.domain.model.User;
import com.esteirahabitacional.shared.CurrentTimeProvider;
import com.esteirahabitacional.shared.IdentifierGenerator;
import java.time.Instant;
import java.util.UUID;

public class ProvisionInitialAdministratorService implements ProvisionInitialAdministratorUseCase {

    private final UserRepository users;
    private final IdentifierGenerator identifiers;
    private final CurrentTimeProvider time;

    public ProvisionInitialAdministratorService(
            UserRepository users, IdentifierGenerator identifiers, CurrentTimeProvider time) {
        this.users = users;
        this.identifiers = identifiers;
        this.time = time;
    }

    @Override
    public Result execute(Command command) {
        Instant occurredAt = time.now();
        UUID userId = identifiers.generate();
        User user = User.provisionPlatformAdministrator(
                userId,
                command.organizationId(),
                new Email(command.email()),
                command.displayName(),
                occurredAt);
        users.save(user);
        users.recordAccessAction(new AccessAction(
                identifiers.generate(), command.organizationId(), null, userId,
                "INITIAL_ADMINISTRATOR_PROVISIONED", occurredAt));
        return new Result(userId);
    }
}
