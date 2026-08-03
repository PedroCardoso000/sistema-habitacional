package com.esteirahabitacional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.esteirahabitacional.identityaccess.adapter.in.web.DevHeaderCurrentActorProvider;
import com.esteirahabitacional.support.PostgresqlTestConfiguration;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
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
class FinancingProcessIT {
    private static final UUID ORGANIZATION_A = UUID.fromString("30000000-0000-0000-0000-000000000001");
    private static final UUID ORGANIZATION_B = UUID.fromString("40000000-0000-0000-0000-000000000002");
    private static final UUID MANAGER_A = UUID.fromString("30000000-0000-0000-0000-000000000011");
    private static final UUID MANAGER_B = UUID.fromString("40000000-0000-0000-0000-000000000021");
    private static final UUID BROKER_A = UUID.fromString("30000000-0000-0000-0000-000000000031");
    private static final UUID CLIENT_A = UUID.fromString("30000000-0000-0000-0000-000000000041");

    @Autowired private MockMvc mockMvc;
    @Autowired private JdbcTemplate jdbc;

    @BeforeEach
    void seed() {
        cleanup();
        seedOrganization(ORGANIZATION_A, "Organization A");
        seedOrganization(ORGANIZATION_B, "Organization B");
        seedUser(MANAGER_A, ORGANIZATION_A, "manager-a@example.com", "MANAGER");
        seedUser(MANAGER_B, ORGANIZATION_B, "manager-b@example.com", "MANAGER");
        seedBroker();
        seedClient();
    }

    @AfterEach
    void cleanup() {
        jdbc.update("DELETE FROM financing_process_audit");
        jdbc.update("DELETE FROM financing_process_property_history");
        jdbc.update("DELETE FROM financing_process_participants");
        jdbc.update("DELETE FROM financing_processes");
        jdbc.update("DELETE FROM financing_process_number_counters");
        jdbc.update("DELETE FROM party_brokers");
        jdbc.update("DELETE FROM party_clients");
        jdbc.update("DELETE FROM access_action_audit");
        jdbc.update("DELETE FROM identity_users");
        jdbc.update("DELETE FROM organizations");
    }

    @Test
    void shouldCreateBothOriginsAndAllowDraftWithoutProperty() throws Exception {
        create(ProcessOriginJson.DIRECT, null).andExpect(status().isCreated())
                .andExpect(jsonPath("$.version").value(0));
        create(ProcessOriginJson.BROKER, BROKER_A).andExpect(status().isCreated());

        mockMvc.perform(authenticated(get("/api/organizations/{organizationId}/processes", ORGANIZATION_A),
                        MANAGER_A, ORGANIZATION_A))
                .andExpect(status().isOk()).andExpect(jsonPath("$.total").value(2))
                .andExpect(jsonPath("$.items[0].status").value("DRAFT"));
        assertThat(jdbc.queryForObject("SELECT count(*) FROM financing_process_property_history", Integer.class))
                .isZero();
    }

    @Test
    void shouldPreservePropertyHistoryAndRejectStaleVersion() throws Exception {
        UUID processId = createdId(create(ProcessOriginJson.DIRECT, null));
        String property = """
                {"addressLine":"Rua A, 10","city":"Fortaleza","state":"CE",
                 "postalCode":"60000-000","expectedVersion":0}
                """;
        mockMvc.perform(authenticated(put(
                        "/api/organizations/{organizationId}/processes/{processId}/property",
                        ORGANIZATION_A, processId), MANAGER_A, ORGANIZATION_A)
                        .contentType(MediaType.APPLICATION_JSON).content(property))
                .andExpect(status().isOk()).andExpect(jsonPath("$.version").value(1));
        mockMvc.perform(authenticated(patch(
                        "/api/organizations/{organizationId}/processes/{processId}/priority",
                        ORGANIZATION_A, processId), MANAGER_A, ORGANIZATION_A)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"priority\":\"HIGH\",\"expectedVersion\":0}"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("PROCESS_VERSION_CONFLICT"));
        assertThat(jdbc.queryForObject("SELECT count(*) FROM financing_process_property_history "
                + "WHERE process_id = ?", Integer.class, processId)).isEqualTo(1);
    }

    @Test
    void shouldEnforceTenantAndKeepDraftInvisibleToBroker() throws Exception {
        UUID processId = createdId(create(ProcessOriginJson.BROKER, BROKER_A));
        mockMvc.perform(authenticated(get(
                        "/api/organizations/{organizationId}/processes/{processId}", ORGANIZATION_B, processId),
                        MANAGER_B, ORGANIZATION_B)).andExpect(status().isNotFound());

        UUID brokerUser = UUID.fromString("30000000-0000-0000-0000-000000000051");
        seedUser(brokerUser, ORGANIZATION_A, "broker-user@example.com", "BROKER");
        mockMvc.perform(authenticated(get(
                        "/api/organizations/{organizationId}/processes/{processId}", ORGANIZATION_A, processId),
                        brokerUser, ORGANIZATION_A)).andExpect(status().isForbidden());
    }

    @Test
    void shouldRejectBrokerOriginWithoutBrokerAndCrossTenantBroker() throws Exception {
        create(ProcessOriginJson.BROKER, null).andExpect(status().isUnprocessableEntity());
        createFor(ORGANIZATION_B, MANAGER_B, ProcessOriginJson.BROKER, BROKER_A)
                .andExpect(status().isNotFound());
    }

    private org.springframework.test.web.servlet.ResultActions create(ProcessOriginJson origin, UUID brokerId)
            throws Exception {
        return createFor(ORGANIZATION_A, MANAGER_A, origin, brokerId);
    }

    private org.springframework.test.web.servlet.ResultActions createFor(
            UUID organization, UUID actor, ProcessOriginJson origin, UUID brokerId) throws Exception {
        String broker = brokerId == null ? "" : ",\"brokerId\":\"" + brokerId + "\"";
        return mockMvc.perform(authenticated(post("/api/organizations/{organizationId}/processes", organization),
                        actor, organization).contentType(MediaType.APPLICATION_JSON)
                .content("{\"origin\":\"" + origin.value + "\"" + broker + "}"));
    }

    private UUID createdId(org.springframework.test.web.servlet.ResultActions action) throws Exception {
        String location = action.andExpect(status().isCreated()).andReturn().getResponse().getHeader("Location");
        return UUID.fromString(location.substring(location.lastIndexOf('/') + 1));
    }

    private MockHttpServletRequestBuilder authenticated(
            MockHttpServletRequestBuilder request, UUID userId, UUID organizationId) {
        return request.header(DevHeaderCurrentActorProvider.USER_HEADER, userId)
                .header(DevHeaderCurrentActorProvider.ORGANIZATION_HEADER, organizationId);
    }

    private void seedOrganization(UUID id, String name) {
        jdbc.update("INSERT INTO organizations (id, name, created_at) VALUES (?, ?, ?)", id, name, time());
    }
    private void seedUser(UUID id, UUID organizationId, String email, String role) {
        jdbc.update("INSERT INTO identity_users (id, organization_id, email, display_name, role, status, "
                        + "created_at, access_changed_at) VALUES (?, ?, ?, ?, ?, 'ACTIVE', ?, ?)",
                id, organizationId, email, email, role, time(), time());
    }
    private void seedBroker() {
        jdbc.update("INSERT INTO party_brokers (id, organization_id, cpf, full_name, email, status, "
                        + "created_at, updated_at) VALUES (?, ?, '52998224725', 'Broker', 'broker@example.com', "
                        + "'ACTIVE', ?, ?)", BROKER_A, ORGANIZATION_A, time(), time());
    }
    private void seedClient() {
        jdbc.update("INSERT INTO party_clients (id, organization_id, cpf, full_name, email, status, "
                        + "created_at, updated_at) VALUES (?, ?, '12345678909', 'Client', 'client@example.com', "
                        + "'ACTIVE', ?, ?)", CLIENT_A, ORGANIZATION_A, time(), time());
    }
    private OffsetDateTime time() { return OffsetDateTime.ofInstant(Instant.EPOCH, ZoneOffset.UTC); }
    private enum ProcessOriginJson {
        DIRECT("DIRECT_CLIENT"), BROKER("BROKER");
        private final String value;
        ProcessOriginJson(String value) { this.value = value; }
    }
}
