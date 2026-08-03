package com.esteirahabitacional.identityaccess.domain.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class UserTest {

    private static final Instant REGISTERED_AT = Instant.parse("2026-08-03T12:00:00Z");

    @Test
    void shouldAuthorizeActiveManagerToManageInternalUsers() {
        User user = internalUser(Role.MANAGER);

        assertThat(user.can(Permission.MANAGE_INTERNAL_USERS)).isTrue();
    }

    @Test
    void shouldDenyEveryPermissionAfterRevocationAndPreserveIdentity() {
        User user = internalUser(Role.MANAGER);
        UUID id = user.id();
        Instant revokedAt = REGISTERED_AT.plusSeconds(60);

        user.revoke(revokedAt);

        assertThat(user.can(Permission.MANAGE_INTERNAL_USERS)).isFalse();
        assertThat(user.id()).isEqualTo(id);
        assertThat(user.status()).isEqualTo(AccessStatus.REVOKED);
        assertThat(user.accessChangedAt()).isEqualTo(revokedAt);
    }

    @Test
    void shouldNeverReactivateOrChangeRevokedAccess() {
        User user = internalUser(Role.MANAGER);
        user.revoke(REGISTERED_AT.plusSeconds(60));

        assertThatThrownBy(() -> user.assignInternalRole(Role.ANALYST, REGISTERED_AT.plusSeconds(120)))
                .isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> user.suspend(REGISTERED_AT.plusSeconds(120)))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void shouldRejectExternalRoleForInternalRegistration() {
        assertThatThrownBy(() -> User.registerInternal(
                        UUID.randomUUID(), UUID.randomUUID(), new Email("broker@example.com"),
                        "Broker", Role.BROKER, REGISTERED_AT))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void shouldProvisionPlatformAdministratorWithoutInventingDocumentPermission() {
        User administrator = User.provisionPlatformAdministrator(
                UUID.randomUUID(), UUID.randomUUID(), new Email("admin@example.com"),
                "Platform Admin", REGISTERED_AT);

        assertThat(administrator.role()).isEqualTo(Role.PLATFORM_ADMIN);
        assertThat(administrator.can(Permission.CREATE_ORGANIZATION)).isTrue();
    }

    private User internalUser(Role role) {
        return User.registerInternal(
                UUID.randomUUID(), UUID.randomUUID(), new Email("manager@example.com"),
                "Manager", role, REGISTERED_AT);
    }
}
