package com.esteirahabitacional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.esteirahabitacional.identityaccess.adapter.in.web.DevHeaderCurrentActorProvider;
import com.esteirahabitacional.parties.BrokerReferenceLookup;
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
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

@SpringBootTest
@AutoConfigureMockMvc
@Import(PostgresqlTestConfiguration.class)
class PartiesIT {

    private static final UUID ORGANIZATION_A = UUID.fromString("10000000-0000-0000-0000-000000000001");
    private static final UUID ORGANIZATION_B = UUID.fromString("20000000-0000-0000-0000-000000000002");
    private static final UUID MANAGER_A = UUID.fromString("10000000-0000-0000-0000-000000000011");
    private static final UUID MANAGER_B = UUID.fromString("20000000-0000-0000-0000-000000000021");

    @Autowired private MockMvc mockMvc;
    @Autowired private JdbcTemplate jdbc;
    @Autowired private BrokerReferenceLookup brokerReferences;

    @BeforeEach
    void resetDatabase() {
        jdbc.update("DELETE FROM party_action_audit");
        jdbc.update("DELETE FROM party_brokers");
        jdbc.update("DELETE FROM party_clients");
        jdbc.update("DELETE FROM party_agencies");
        jdbc.update("DELETE FROM platform_administration_audit");
        jdbc.update("DELETE FROM access_action_audit");
        jdbc.update("DELETE FROM identity_users");
        jdbc.update("DELETE FROM organizations");
        seedOrganization(ORGANIZATION_A, "Organization A");
        seedOrganization(ORGANIZATION_B, "Organization B");
        seedManager(MANAGER_A, ORGANIZATION_A, "manager-a@example.com");
        seedManager(MANAGER_B, ORGANIZATION_B, "manager-b@example.com");
    }

    @Test
    void shouldPreventDuplicateInsideOrganizationAndAllowSameCpfInAnotherOrganization() throws Exception {
        registerClient(ORGANIZATION_A, MANAGER_A, "Tenant A Client").andExpect(status().isCreated());
        registerClient(ORGANIZATION_A, MANAGER_A, "Duplicate")
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("PARTY_ALREADY_REGISTERED"));
        registerClient(ORGANIZATION_B, MANAGER_B, "Tenant B Client").andExpect(status().isCreated());

        assertThat(count("party_clients", ORGANIZATION_A)).isEqualTo(1);
        assertThat(count("party_clients", ORGANIZATION_B)).isEqualTo(1);
    }

    @Test
    void shouldDenyManipulatedOrganizationIdWithoutCrossingTenant() throws Exception {
        mockMvc.perform(authenticated(post("/api/organizations/{organizationId}/parties/clients", ORGANIZATION_B),
                        MANAGER_A, ORGANIZATION_A)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(clientJson("Cross Tenant")))
                .andExpect(status().isForbidden());

        assertThat(count("party_clients", ORGANIZATION_B)).isZero();
    }

    @Test
    void shouldFindTenantClientWithoutExposingCpf() throws Exception {
        registerClient(ORGANIZATION_A, MANAGER_A, "Tenant A Client").andExpect(status().isCreated());
        registerClient(ORGANIZATION_B, MANAGER_B, "Tenant B Client").andExpect(status().isCreated());

        mockMvc.perform(authenticated(
                        post("/api/organizations/{organizationId}/parties/clients/search", ORGANIZATION_A),
                        MANAGER_A, ORGANIZATION_A)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"cpf\":\"529.982.247-25\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.fullName").value("Tenant A Client"))
                .andExpect(jsonPath("$.cpf").doesNotExist());
    }

    @Test
    void shouldManagePartnersAndRejectInactiveBrokerForProcessReference() throws Exception {
        String agencyLocation = mockMvc.perform(authenticated(
                        post("/api/organizations/{organizationId}/parties/agencies", ORGANIZATION_A),
                        MANAGER_A, ORGANIZATION_A)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"cnpj":"11.222.333/0001-81","legalName":"Agency",
                                 "email":"agency@example.com"}
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.cnpj").doesNotExist())
                .andReturn().getResponse().getHeader("Location");
        UUID agencyId = locationId(agencyLocation);

        String brokerLocation = mockMvc.perform(authenticated(
                        post("/api/organizations/{organizationId}/parties/brokers", ORGANIZATION_A),
                        MANAGER_A, ORGANIZATION_A)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"cpf":"123.456.789-09","name":"Broker",
                                 "email":"broker@example.com"}
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.cpf").doesNotExist())
                .andReturn().getResponse().getHeader("Location");
        UUID brokerId = locationId(brokerLocation);

        mockMvc.perform(authenticated(put(
                        "/api/organizations/{organizationId}/parties/brokers/{brokerId}/agency",
                        ORGANIZATION_A, brokerId), MANAGER_A, ORGANIZATION_A)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"agencyId\":\"" + agencyId + "\"}"))
                .andExpect(status().isNoContent());

        mockMvc.perform(authenticated(put(
                        "/api/organizations/{organizationId}/parties/partners/BROKER/{brokerId}/status",
                        ORGANIZATION_A, brokerId), MANAGER_A, ORGANIZATION_A)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\":\"INACTIVE\"}"))
                .andExpect(status().isNoContent());

        assertThatThrownBy(() -> brokerReferences.findActive(ORGANIZATION_A, brokerId))
                .isInstanceOf(ApplicationException.class)
                .extracting(exception -> ((ApplicationException) exception).status())
                .isEqualTo(422);
    }

    @Test
    void shouldUpdateContactAndListMinimalPaginatedProjection() throws Exception {
        String location = registerClient(ORGANIZATION_A, MANAGER_A, "Client")
                .andExpect(status().isCreated())
                .andReturn().getResponse().getHeader("Location");
        UUID clientId = locationId(location);

        mockMvc.perform(authenticated(patch(
                        "/api/organizations/{organizationId}/parties/CLIENT/{clientId}/contact",
                        ORGANIZATION_A, clientId), MANAGER_A, ORGANIZATION_A)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"phone\":\"(85) 98888-7777\"}"))
                .andExpect(status().isNoContent());

        mockMvc.perform(authenticated(get("/api/organizations/{organizationId}/parties", ORGANIZATION_A),
                        MANAGER_A, ORGANIZATION_A)
                        .queryParam("type", "CLIENT")
                        .queryParam("page", "0")
                        .queryParam("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.total").value(1))
                .andExpect(jsonPath("$.items[0].name").value("Client"))
                .andExpect(jsonPath("$.items[0].cpf").doesNotExist())
                .andExpect(jsonPath("$.items[0].email").doesNotExist())
                .andExpect(jsonPath("$.items[0].phone").doesNotExist());

        assertThat(jdbc.queryForObject(
                "SELECT phone FROM party_clients WHERE organization_id = ? AND id = ?",
                String.class, ORGANIZATION_A, clientId)).isEqualTo("85988887777");
        assertThat(jdbc.queryForObject(
                "SELECT count(*) FROM party_action_audit WHERE organization_id = ? AND target_id = ?",
                Integer.class, ORGANIZATION_A, clientId)).isEqualTo(2);
    }

    private org.springframework.test.web.servlet.ResultActions registerClient(
            UUID organizationId, UUID actorId, String name) throws Exception {
        return mockMvc.perform(authenticated(
                        post("/api/organizations/{organizationId}/parties/clients", organizationId),
                        actorId, organizationId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(clientJson(name)));
    }

    private MockHttpServletRequestBuilder authenticated(
            MockHttpServletRequestBuilder request, UUID userId, UUID organizationId) {
        return request.header(DevHeaderCurrentActorProvider.USER_HEADER, userId)
                .header(DevHeaderCurrentActorProvider.ORGANIZATION_HEADER, organizationId);
    }

    private String clientJson(String name) {
        return "{\"cpf\":\"529.982.247-25\",\"name\":\"" + name
                + "\",\"email\":\"client@example.com\"}";
    }

    private UUID locationId(String location) {
        return UUID.fromString(location.substring(location.lastIndexOf('/') + 1));
    }

    private int count(String table, UUID organizationId) {
        return jdbc.queryForObject(
                "SELECT count(*) FROM " + table + " WHERE organization_id = ?",
                Integer.class, organizationId);
    }

    private void seedOrganization(UUID id, String name) {
        jdbc.update("INSERT INTO organizations (id, name, created_at) VALUES (?, ?, ?)",
                id, name, OffsetDateTime.ofInstant(Instant.EPOCH, ZoneOffset.UTC));
    }

    private void seedManager(UUID id, UUID organizationId, String email) {
        OffsetDateTime instant = OffsetDateTime.ofInstant(Instant.EPOCH, ZoneOffset.UTC);
        jdbc.update("INSERT INTO identity_users "
                        + "(id, organization_id, email, display_name, role, status, created_at, access_changed_at) "
                        + "VALUES (?, ?, ?, ?, 'MANAGER', 'ACTIVE', ?, ?)",
                id, organizationId, email, email, instant, instant);
    }
}
