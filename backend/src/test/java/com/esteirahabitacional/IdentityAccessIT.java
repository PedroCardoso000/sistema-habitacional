package com.esteirahabitacional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.esteirahabitacional.identityaccess.adapter.in.web.DevHeaderCurrentActorProvider;
import com.esteirahabitacional.platformadministration.application.port.in.BootstrapFirstOrganizationUseCase;
import com.esteirahabitacional.shared.ApplicationException;
import com.esteirahabitacional.support.PostgresqlTestConfiguration;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest(properties = {
    "app.bootstrap.enabled=true",
    "app.bootstrap.expected-secret=test-secret",
    "app.platform-administration.organization-creation-enabled=true"
})
@AutoConfigureMockMvc
@Import(PostgresqlTestConfiguration.class)
class IdentityAccessIT {

    private static final UUID ORGANIZATION_A = UUID.fromString("10000000-0000-0000-0000-000000000001");
    private static final UUID ORGANIZATION_B = UUID.fromString("20000000-0000-0000-0000-000000000002");
    private static final UUID MANAGER_A = UUID.fromString("10000000-0000-0000-0000-000000000011");
    private static final UUID ANALYST_A = UUID.fromString("10000000-0000-0000-0000-000000000012");
    private static final UUID MANAGER_B = UUID.fromString("20000000-0000-0000-0000-000000000021");

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JdbcTemplate jdbc;

    @Autowired
    private BootstrapFirstOrganizationUseCase bootstrap;

    @BeforeEach
    void resetDatabase() {
        jdbc.update("DELETE FROM platform_administration_audit");
        jdbc.update("DELETE FROM access_action_audit");
        jdbc.update("DELETE FROM identity_users");
        jdbc.update("DELETE FROM organizations");
    }

    @Test
    void shouldIsolateOrganizationsWhenExternalOrganizationIdIsManipulated() throws Exception {
        seedOrganization(ORGANIZATION_A, "Organization A");
        seedOrganization(ORGANIZATION_B, "Organization B");
        seedUser(MANAGER_A, ORGANIZATION_A, "manager-a@example.com", "MANAGER", "ACTIVE");
        seedUser(MANAGER_B, ORGANIZATION_B, "manager-b@example.com", "MANAGER", "ACTIVE");

        mockMvc.perform(post("/api/organizations/{organizationId}/users", ORGANIZATION_B)
                        .header(DevHeaderCurrentActorProvider.USER_HEADER, MANAGER_A)
                        .header(DevHeaderCurrentActorProvider.ORGANIZATION_HEADER, ORGANIZATION_A)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email":"intruder@example.com","displayName":"Intruder","role":"ANALYST"}
                                """))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("ACCESS_DENIED"));

        assertThat(countUsers(ORGANIZATION_B)).isEqualTo(1);
    }

    @Test
    void shouldRejectRevokedAccessAndInsufficientRole() throws Exception {
        seedOrganization(ORGANIZATION_A, "Organization A");
        seedUser(MANAGER_A, ORGANIZATION_A, "manager@example.com", "MANAGER", "REVOKED");
        seedUser(ANALYST_A, ORGANIZATION_A, "analyst@example.com", "ANALYST", "ACTIVE");

        mockMvc.perform(get("/api/identity/context")
                        .header(DevHeaderCurrentActorProvider.USER_HEADER, MANAGER_A)
                        .header(DevHeaderCurrentActorProvider.ORGANIZATION_HEADER, ORGANIZATION_A))
                .andExpect(status().isForbidden());

        mockMvc.perform(post("/api/organizations/{organizationId}/users", ORGANIZATION_A)
                        .header(DevHeaderCurrentActorProvider.USER_HEADER, ANALYST_A)
                        .header(DevHeaderCurrentActorProvider.ORGANIZATION_HEADER, ORGANIZATION_A)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email":"new@example.com","displayName":"New User","role":"ANALYST"}
                                """))
                .andExpect(status().isForbidden());
    }

    @Test
    void shouldRegisterAndRevokeInternalUserWhilePreservingAuthorship() throws Exception {
        seedOrganization(ORGANIZATION_A, "Organization A");
        seedUser(MANAGER_A, ORGANIZATION_A, "manager@example.com", "MANAGER", "ACTIVE");

        String location = mockMvc.perform(post("/api/organizations/{organizationId}/users", ORGANIZATION_A)
                        .header(DevHeaderCurrentActorProvider.USER_HEADER, MANAGER_A)
                        .header(DevHeaderCurrentActorProvider.ORGANIZATION_HEADER, ORGANIZATION_A)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email":"new@example.com","displayName":"New Analyst","role":"ANALYST"}
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.organizationId").value(ORGANIZATION_A.toString()))
                .andReturn().getResponse().getHeader("Location");
        UUID userId = UUID.fromString(location.substring(location.lastIndexOf('/') + 1));

        mockMvc.perform(post("/api/organizations/{organizationId}/users/{userId}/revocation",
                        ORGANIZATION_A, userId)
                        .header(DevHeaderCurrentActorProvider.USER_HEADER, MANAGER_A)
                        .header(DevHeaderCurrentActorProvider.ORGANIZATION_HEADER, ORGANIZATION_A))
                .andExpect(status().isNoContent());

        assertThat(jdbc.queryForObject(
                "SELECT status FROM identity_users WHERE organization_id = ? AND id = ?",
                String.class, ORGANIZATION_A, userId)).isEqualTo("REVOKED");
        assertThat(jdbc.queryForObject(
                "SELECT actor_user_id FROM access_action_audit WHERE organization_id = ? "
                        + "AND target_user_id = ? AND action = 'ACCESS_REVOKED'",
                UUID.class, ORGANIZATION_A, userId)).isEqualTo(MANAGER_A);
    }

    @Test
    void shouldCreateOrganizationOnlyForAuthorizedPlatformAdministrator() throws Exception {
        seedOrganization(ORGANIZATION_A, "Initial Organization");
        seedUser(MANAGER_A, ORGANIZATION_A, "platform@example.com", "PLATFORM_ADMIN", "ACTIVE");

        mockMvc.perform(post("/api/platform/organizations")
                        .header(DevHeaderCurrentActorProvider.USER_HEADER, MANAGER_A)
                        .header(DevHeaderCurrentActorProvider.ORGANIZATION_HEADER, ORGANIZATION_A)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Second Organization\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Second Organization"));

        assertThat(jdbc.queryForObject("SELECT count(*) FROM organizations", Integer.class)).isEqualTo(2);
        assertThat(jdbc.queryForObject(
                "SELECT count(*) FROM platform_administration_audit WHERE actor_organization_id = ? "
                        + "AND actor_user_id = ? AND action = 'ORGANIZATION_CREATED'",
                Integer.class, ORGANIZATION_A, MANAGER_A)).isEqualTo(1);
    }

    @Test
    void shouldRejectUnauthorizedAndRepeatedBootstrap() {
        BootstrapFirstOrganizationUseCase.Command unauthorized = bootstrapCommand("wrong-secret");

        assertThatThrownBy(() -> bootstrap.execute(unauthorized))
                .isInstanceOf(ApplicationException.class)
                .extracting(exception -> ((ApplicationException) exception).status())
                .isEqualTo(403);
        assertThat(jdbc.queryForObject("SELECT count(*) FROM organizations", Integer.class)).isZero();

        bootstrap.execute(bootstrapCommand("test-secret"));

        assertThatThrownBy(() -> bootstrap.execute(bootstrapCommand("test-secret")))
                .isInstanceOf(ApplicationException.class)
                .extracting(exception -> ((ApplicationException) exception).status())
                .isEqualTo(409);
        assertThat(jdbc.queryForObject("SELECT count(*) FROM organizations", Integer.class)).isEqualTo(1);
        assertThat(jdbc.queryForObject("SELECT count(*) FROM identity_users", Integer.class)).isEqualTo(1);
    }

    private BootstrapFirstOrganizationUseCase.Command bootstrapCommand(String secret) {
        return new BootstrapFirstOrganizationUseCase.Command(
                secret, "Initial Organization", "admin@example.com", "Platform Administrator");
    }

    private void seedOrganization(UUID id, String name) {
        jdbc.update("INSERT INTO organizations (id, name, created_at) VALUES (?, ?, ?)",
                id, name, OffsetDateTime.ofInstant(Instant.EPOCH, ZoneOffset.UTC));
    }

    private void seedUser(UUID id, UUID organizationId, String email, String role, String status) {
        jdbc.update("INSERT INTO identity_users "
                        + "(id, organization_id, email, display_name, role, status, created_at, access_changed_at) "
                        + "VALUES (?, ?, ?, ?, ?, ?, ?, ?)",
                id, organizationId, email, email, role, status,
                OffsetDateTime.ofInstant(Instant.EPOCH, ZoneOffset.UTC),
                OffsetDateTime.ofInstant(Instant.EPOCH, ZoneOffset.UTC));
    }

    private int countUsers(UUID organizationId) {
        return jdbc.queryForObject(
                "SELECT count(*) FROM identity_users WHERE organization_id = ?",
                Integer.class, organizationId);
    }
}
