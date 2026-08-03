package com.esteirahabitacional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.esteirahabitacional.identityaccess.adapter.in.web.DevHeaderCurrentActorProvider;
import com.esteirahabitacional.shared.ApplicationException;
import com.esteirahabitacional.support.PostgresqlTestConfiguration;
import com.esteirahabitacional.workflow.InitializeWorkflowForSubmissionUseCase;
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
class WorkflowIT {
    private static final UUID ORGANIZATION_A = UUID.fromString("50000000-0000-0000-0000-000000000001");
    private static final UUID ORGANIZATION_B = UUID.fromString("60000000-0000-0000-0000-000000000002");
    private static final UUID MANAGER_A = UUID.fromString("50000000-0000-0000-0000-000000000011");
    private static final UUID MANAGER_B = UUID.fromString("60000000-0000-0000-0000-000000000021");
    private static final UUID ANALYST_A = UUID.fromString("50000000-0000-0000-0000-000000000022");
    private static final UUID ACTIVE_PROCESS = UUID.fromString("50000000-0000-0000-0000-000000000031");
    private static final UUID DRAFT_PROCESS = UUID.fromString("50000000-0000-0000-0000-000000000041");

    @Autowired private MockMvc mockMvc;
    @Autowired private JdbcTemplate jdbc;
    @Autowired private InitializeWorkflowForSubmissionUseCase initialization;

    @BeforeEach
    void seed() throws Exception {
        cleanup();
        seedOrganization(ORGANIZATION_A, "Organization A");
        seedOrganization(ORGANIZATION_B, "Organization B");
        seedUser(MANAGER_A, ORGANIZATION_A, "manager-a@example.com");
        seedUser(MANAGER_B, ORGANIZATION_B, "manager-b@example.com");
        seedUser(ANALYST_A, ORGANIZATION_A, "analyst-a@example.com", "ANALYST");
        seedProcess(ACTIVE_PROCESS, ORGANIZATION_A, MANAGER_A, "FP-000001", "ACTIVE");
        seedProcess(DRAFT_PROCESS, ORGANIZATION_A, MANAGER_A, "FP-000002", "DRAFT");
        mockMvc.perform(authenticated(put(
                        "/api/organizations/{organizationId}/workflow/models/initial", ORGANIZATION_A),
                        MANAGER_A, ORGANIZATION_A)).andExpect(status().isOk())
                .andExpect(jsonPath("$.version").value(1)).andExpect(jsonPath("$.stageCount").value(6));
    }

    @AfterEach
    void cleanup() {
        jdbc.update("DELETE FROM workflow_audit");
        jdbc.update("DELETE FROM workflow_stage_transitions");
        jdbc.update("DELETE FROM workflow_process_stages");
        jdbc.update("DELETE FROM workflow_journeys");
        jdbc.update("DELETE FROM workflow_stage_definitions");
        jdbc.update("DELETE FROM workflow_models");
        jdbc.update("DELETE FROM financing_process_audit");
        jdbc.update("DELETE FROM financing_process_participants");
        jdbc.update("DELETE FROM financing_process_property_history");
        jdbc.update("DELETE FROM financing_processes");
        jdbc.update("DELETE FROM financing_process_number_counters");
        jdbc.update("DELETE FROM access_action_audit");
        jdbc.update("DELETE FROM identity_users");
        jdbc.update("DELETE FROM organizations");
    }

    @Test
    void shouldRejectDraftOutsideSubmissionAndInitializeActiveProcess() throws Exception {
        assertThatThrownBy(() -> initialization.initialize(
                new InitializeWorkflowForSubmissionUseCase.Command(ORGANIZATION_A, DRAFT_PROCESS, MANAGER_A)))
                .isInstanceOf(ApplicationException.class)
                .extracting(exception -> ((ApplicationException) exception).status()).isEqualTo(422);

        mockMvc.perform(authenticated(patch(
                        "/api/organizations/{organizationId}/processes/{processId}/priority",
                        ORGANIZATION_A, ACTIVE_PROCESS), MANAGER_A, ORGANIZATION_A)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"priority\":\"HIGH\",\"expectedVersion\":0}"))
                .andExpect(status().isUnprocessableEntity());

        initialization.initialize(
                new InitializeWorkflowForSubmissionUseCase.Command(ORGANIZATION_A, ACTIVE_PROCESS, MANAGER_A));
        mockMvc.perform(authenticated(get(
                        "/api/organizations/{organizationId}/processes/{processId}/workflow",
                        ORGANIZATION_A, ACTIVE_PROCESS), MANAGER_A, ORGANIZATION_A))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.workflowVersion").value(1))
                .andExpect(jsonPath("$.currentStage.code").value("INITIAL_REVIEW"))
                .andExpect(jsonPath("$.missingNextAction").value(true));
    }

    @Test
    void shouldManageNextActionBlockAdvanceReturnAndHistory() throws Exception {
        initializeActive();
        mockMvc.perform(authenticated(put(
                        "/api/organizations/{organizationId}/processes/{processId}/workflow/next-action",
                        ORGANIZATION_A, ACTIVE_PROCESS), MANAGER_A, ORGANIZATION_A)
                        .contentType(MediaType.APPLICATION_JSON).content("""
                                {"description":"Revisar cadastro","responsibleUserId":"%s",
                                 "expectedVersion":0}
                                """.formatted(MANAGER_A)))
                .andExpect(status().isOk()).andExpect(jsonPath("$.version").value(1));
        mockMvc.perform(authenticated(patch(
                        "/api/organizations/{organizationId}/processes/{processId}/workflow/advance",
                        ORGANIZATION_A, ACTIVE_PROCESS), MANAGER_A, ORGANIZATION_A)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"satisfiedCriteria\":[],\"expectedVersion\":1}"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.currentStageCode").value("BUYER_DOCUMENTS"));
        mockMvc.perform(authenticated(patch(
                        "/api/organizations/{organizationId}/processes/{processId}/workflow/return",
                        ORGANIZATION_A, ACTIVE_PROCESS), MANAGER_A, ORGANIZATION_A)
                        .contentType(MediaType.APPLICATION_JSON).content("""
                                {"targetStageCode":"INITIAL_REVIEW","justification":"Reanálise necessária",
                                 "expectedVersion":2}
                                """))
                .andExpect(status().isOk()).andExpect(jsonPath("$.currentStageCode").value("INITIAL_REVIEW"));

        mockMvc.perform(authenticated(get(
                        "/api/organizations/{organizationId}/processes/{processId}/workflow",
                        ORGANIZATION_A, ACTIVE_PROCESS), MANAGER_A, ORGANIZATION_A))
                .andExpect(status().isOk()).andExpect(jsonPath("$.transitions.length()").value(3))
                .andExpect(jsonPath("$.transitions[2].type").value("RETURNED"))
                .andExpect(jsonPath("$.missingNextAction").value(true));
        assertThat(jdbc.queryForObject("SELECT count(*) FROM workflow_audit WHERE process_id = ?",
                Integer.class, ACTIVE_PROCESS)).isEqualTo(4);
    }

    @Test
    void shouldRejectBlockedAdvanceAndStaleVersion() throws Exception {
        initializeActive();
        mockMvc.perform(authenticated(patch(
                        "/api/organizations/{organizationId}/processes/{processId}/workflow/block",
                        ORGANIZATION_A, ACTIVE_PROCESS), MANAGER_A, ORGANIZATION_A)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"justification\":\"Aguardando validação\",\"expectedVersion\":0}"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.version").value(1));
        mockMvc.perform(authenticated(patch(
                        "/api/organizations/{organizationId}/processes/{processId}/workflow/advance",
                        ORGANIZATION_A, ACTIVE_PROCESS), MANAGER_A, ORGANIZATION_A)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"satisfiedCriteria\":[],\"expectedVersion\":1}"))
                .andExpect(status().isUnprocessableEntity());
        mockMvc.perform(authenticated(patch(
                        "/api/organizations/{organizationId}/processes/{processId}/workflow/unblock",
                        ORGANIZATION_A, ACTIVE_PROCESS), MANAGER_A, ORGANIZATION_A)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"justification\":\"Validado\",\"expectedVersion\":0}"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("WORKFLOW_VERSION_CONFLICT"));
    }

    @Test
    void shouldEnforceTenantIsolationAndResponsibleUser() throws Exception {
        initializeActive();
        mockMvc.perform(authenticated(get(
                        "/api/organizations/{organizationId}/processes/{processId}/workflow",
                        ORGANIZATION_B, ACTIVE_PROCESS), MANAGER_B, ORGANIZATION_B))
                .andExpect(status().isNotFound());
        mockMvc.perform(authenticated(patch(
                        "/api/organizations/{organizationId}/processes/{processId}/workflow/exception",
                        ORGANIZATION_A, ACTIVE_PROCESS), ANALYST_A, ORGANIZATION_A)
                        .contentType(MediaType.APPLICATION_JSON).content("""
                                {"targetStageCode":"PRE_APPROVAL","justification":"Exceção solicitada",
                                 "expectedVersion":0}
                                """))
                .andExpect(status().isForbidden());
        mockMvc.perform(authenticated(put(
                        "/api/organizations/{organizationId}/processes/{processId}/workflow/next-action",
                        ORGANIZATION_A, ACTIVE_PROCESS), MANAGER_A, ORGANIZATION_A)
                        .contentType(MediaType.APPLICATION_JSON).content("""
                                {"description":"Revisar cadastro","responsibleUserId":"%s",
                                 "expectedVersion":0}
                                """.formatted(MANAGER_B)))
                .andExpect(status().isNotFound());
    }

    private void initializeActive() {
        initialization.initialize(
                new InitializeWorkflowForSubmissionUseCase.Command(ORGANIZATION_A, ACTIVE_PROCESS, MANAGER_A));
    }
    private MockHttpServletRequestBuilder authenticated(
            MockHttpServletRequestBuilder request, UUID userId, UUID organizationId) {
        return request.header(DevHeaderCurrentActorProvider.USER_HEADER, userId)
                .header(DevHeaderCurrentActorProvider.ORGANIZATION_HEADER, organizationId);
    }
    private void seedOrganization(UUID id, String name) {
        jdbc.update("INSERT INTO organizations (id, name, created_at) VALUES (?, ?, ?)", id, name, time());
    }
    private void seedUser(UUID id, UUID organizationId, String email) {
        seedUser(id, organizationId, email, "MANAGER");
    }
    private void seedUser(UUID id, UUID organizationId, String email, String role) {
        jdbc.update("INSERT INTO identity_users (id, organization_id, email, display_name, role, status, "
                        + "created_at, access_changed_at) VALUES (?, ?, ?, ?, ?, 'ACTIVE', ?, ?)",
                id, organizationId, email, email, role, time(), time());
    }
    private void seedProcess(UUID id, UUID organizationId, UUID actorId, String number, String status) {
        jdbc.update("INSERT INTO financing_processes (id, organization_id, process_number, origin, status, "
                        + "author_user_id, responsible_user_id, priority, version, created_at, updated_at) "
                        + "VALUES (?, ?, ?, 'DIRECT_CLIENT', ?, ?, ?, 'NORMAL', 0, ?, ?)",
                id, organizationId, number, status, actorId, actorId, time(), time());
    }
    private OffsetDateTime time() { return OffsetDateTime.ofInstant(Instant.EPOCH, ZoneOffset.UTC); }
}
