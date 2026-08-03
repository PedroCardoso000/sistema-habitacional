package com.esteirahabitacional.identityaccess.application.port.out;

import com.esteirahabitacional.identityaccess.domain.model.Email;
import com.esteirahabitacional.identityaccess.domain.model.User;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

public interface UserRepository {

    boolean existsByEmail(UUID organizationId, Email email);

    Optional<User> findById(UUID organizationId, UUID userId);

    void save(User user);

    void recordAccessAction(AccessAction action);

    record AccessAction(
            UUID id,
            UUID organizationId,
            UUID actorUserId,
            UUID targetUserId,
            String action,
            Instant occurredAt) {}
}
