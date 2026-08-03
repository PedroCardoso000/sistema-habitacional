package com.esteirahabitacional.identityaccess.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.esteirahabitacional.identityaccess.application.port.out.CurrentActorProvider;
import com.esteirahabitacional.identityaccess.application.port.out.UserRepository;
import com.esteirahabitacional.identityaccess.domain.model.Email;
import com.esteirahabitacional.identityaccess.domain.model.Permission;
import com.esteirahabitacional.identityaccess.domain.model.Role;
import com.esteirahabitacional.identityaccess.domain.model.User;
import com.esteirahabitacional.shared.ApplicationException;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class AuthorizationServiceTest {

    private static final UUID ORGANIZATION_ID = UUID.randomUUID();
    private static final UUID OTHER_ORGANIZATION_ID = UUID.randomUUID();
    private static final UUID USER_ID = UUID.randomUUID();

    @Test
    void shouldAuthorizeActiveManagerInsideOwnOrganization() {
        User manager = user(Role.MANAGER);
        AuthorizationService service = service(manager, ORGANIZATION_ID);

        assertThat(service.require(ORGANIZATION_ID, Permission.MANAGE_INTERNAL_USERS)).isSameAs(manager);
    }

    @Test
    void shouldRejectExternalOrganizationManipulationBeforeAccessingTargetTenant() {
        RecordingUserRepository users = new RecordingUserRepository(user(Role.MANAGER));
        AuthorizationService service = new AuthorizationService(
                () -> new CurrentActorProvider.Actor(USER_ID, ORGANIZATION_ID), users);

        assertThatThrownBy(() -> service.require(OTHER_ORGANIZATION_ID, Permission.MANAGE_INTERNAL_USERS))
                .isInstanceOf(ApplicationException.class)
                .extracting(exception -> ((ApplicationException) exception).status())
                .isEqualTo(403);
        assertThat(users.lastQueriedOrganization).isNull();
    }

    @Test
    void shouldRejectInsufficientRole() {
        AuthorizationService service = service(user(Role.ANALYST), ORGANIZATION_ID);

        assertThatThrownBy(() -> service.require(ORGANIZATION_ID, Permission.MANAGE_INTERNAL_USERS))
                .isInstanceOf(ApplicationException.class);
    }

    private AuthorizationService service(User user, UUID actorOrganizationId) {
        return new AuthorizationService(
                () -> new CurrentActorProvider.Actor(USER_ID, actorOrganizationId),
                new RecordingUserRepository(user));
    }

    private User user(Role role) {
        return User.registerInternal(
                USER_ID, ORGANIZATION_ID, new Email("actor@example.com"), "Actor", role, Instant.EPOCH);
    }

    private static final class RecordingUserRepository implements UserRepository {

        private final User user;
        private UUID lastQueriedOrganization;

        private RecordingUserRepository(User user) {
            this.user = user;
        }

        @Override
        public boolean existsByEmail(UUID organizationId, Email email) {
            return false;
        }

        @Override
        public Optional<User> findById(UUID organizationId, UUID userId) {
            lastQueriedOrganization = organizationId;
            return Optional.ofNullable(user);
        }

        @Override
        public void save(User user) {}

        @Override
        public void recordAccessAction(AccessAction action) {}
    }
}
