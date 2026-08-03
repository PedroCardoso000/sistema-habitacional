package com.esteirahabitacional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.esteirahabitacional.documents.application.port.in.ManageDocumentsUseCase;
import com.esteirahabitacional.identityaccess.adapter.in.web.DevHeaderCurrentActorProvider;
import com.esteirahabitacional.support.PostgresqlTestConfiguration;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.nio.charset.StandardCharsets;
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
class DocumentsIT {
    private static final UUID ORGANIZATION_A = UUID.fromString("70000000-0000-0000-0000-000000000001");
    private static final UUID ORGANIZATION_B = UUID.fromString("80000000-0000-0000-0000-000000000002");
    private static final UUID MANAGER_A = UUID.fromString("70000000-0000-0000-0000-000000000011");
    private static final UUID MANAGER_B = UUID.fromString("80000000-0000-0000-0000-000000000021");
    private static final UUID ANALYST_A = UUID.fromString("70000000-0000-0000-0000-000000000031");
    private static final UUID CLIENT_A = UUID.fromString("70000000-0000-0000-0000-000000000041");
    private static final UUID BROKER_A = UUID.fromString("70000000-0000-0000-0000-000000000051");
    private static final UUID THIRD_PARTY = UUID.fromString("70000000-0000-0000-0000-000000000061");
    private static final UUID PROCESS = UUID.fromString("70000000-0000-0000-0000-000000000071");
    private static final byte[] PDF = "%PDF-1.4\nprivate document".getBytes(StandardCharsets.UTF_8);

    @Autowired private MockMvc mockMvc;
    @Autowired private JdbcTemplate jdbc;
    private final ObjectMapper json = new ObjectMapper();
    @Autowired private ManageDocumentsUseCase documents;

    @BeforeEach
    void seed() {
        cleanup();
        seedOrganization(ORGANIZATION_A, "Organization A");
        seedOrganization(ORGANIZATION_B, "Organization B");
        seedUser(MANAGER_A, ORGANIZATION_A, "manager-a@example.com", "MANAGER");
        seedUser(MANAGER_B, ORGANIZATION_B, "manager-b@example.com", "MANAGER");
        seedUser(ANALYST_A, ORGANIZATION_A, "analyst-a@example.com", "ANALYST");
        seedUser(CLIENT_A, ORGANIZATION_A, "client-a@example.com", "CLIENT");
        seedUser(BROKER_A, ORGANIZATION_A, "broker-a@example.com", "BROKER");
        seedUser(THIRD_PARTY, ORGANIZATION_A, "third@example.com", "CLIENT");
        seedDraft();
    }

    @AfterEach
    void cleanup() {
        jdbc.update("DELETE FROM document_audit");
        jdbc.update("DELETE FROM document_download_grants");
        jdbc.update("DELETE FROM document_versions");
        jdbc.update("DELETE FROM document_upload_intents");
        jdbc.update("DELETE FROM document_requests");
        jdbc.update("DELETE FROM document_checklist_template_items");
        jdbc.update("DELETE FROM document_checklist_templates");
        jdbc.update("DELETE FROM document_types");
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
    void shouldSubmitDraftAtomicallyWithWorkflowChecklistAndNextAction() throws Exception {
        submit().andExpect(status().isOk()).andExpect(jsonPath("$.processStatus").value("ACTIVE"))
                .andExpect(jsonPath("$.currentStageCode").value("INITIAL_REVIEW"))
                .andExpect(jsonPath("$.checklistSize").value(3)).andExpect(jsonPath("$.processVersion").value(1));
        assertThat(jdbc.queryForObject("SELECT status FROM financing_processes WHERE id = ?", String.class, PROCESS))
                .isEqualTo("ACTIVE");
        assertThat(jdbc.queryForObject("SELECT count(*) FROM document_requests WHERE process_id = ?",
                Integer.class, PROCESS)).isEqualTo(3);
        assertThat(jdbc.queryForObject("SELECT next_action_description FROM workflow_journeys WHERE process_id = ?",
                String.class, PROCESS)).isEqualTo("Realizar triagem inicial");
    }

    @Test
    void shouldRollbackSubmissionWhenWorkflowInitializationFails() throws Exception {
        seedWorkflowModelAndExistingJourney();
        submit().andExpect(status().isConflict());
        assertDraftWithoutGeneratedChecklistOrAudit();
        assertThat(jdbc.queryForObject("SELECT count(*) FROM workflow_journeys WHERE process_id = ?",
                Integer.class, PROCESS)).isEqualTo(1);
    }

    @Test
    void shouldRollbackWorkflowAndActivationWhenChecklistGenerationFails() throws Exception {
        seedConflictingChecklistRequest();
        submit().andExpect(status().isInternalServerError());
        assertThat(jdbc.queryForObject("SELECT status FROM financing_processes WHERE id = ?", String.class, PROCESS))
                .isEqualTo("DRAFT");
        assertThat(jdbc.queryForObject("SELECT count(*) FROM workflow_journeys WHERE process_id = ?",
                Integer.class, PROCESS)).isZero();
        assertThat(jdbc.queryForObject("SELECT count(*) FROM document_requests WHERE process_id = ?",
                Integer.class, PROCESS)).isEqualTo(1);
    }

    @Test
    void shouldCompleteDocumentCyclePreserveVersionsAndProtectDownload() throws Exception {
        submit().andExpect(status().isOk());
        JsonNode request = firstRequest();
        UUID requestId = UUID.fromString(request.get("id").asText());

        Upload uploadOne = createAndStoreUpload(requestId, CLIENT_A, "identity.pdf", PDF);
        complete(uploadOne.id(), CLIENT_A).andExpect(status().isOk())
                .andExpect(jsonPath("$.versionCount").value(1)).andExpect(jsonPath("$.version").value(1));
        complete(uploadOne.id(), CLIENT_A).andExpect(status().isOk())
                .andExpect(jsonPath("$.versionCount").value(1));
        mutate(requestId, "review", ANALYST_A, "{\"expectedVersion\":1}")
                .andExpect(status().isOk()).andExpect(jsonPath("$.status").value("UNDER_REVIEW"));
        mutate(requestId, "rejection", ANALYST_A,
                "{\"reason\":\"Imagem ilegível\",\"expectedVersion\":2}")
                .andExpect(status().isOk()).andExpect(jsonPath("$.status").value("REJECTED"));
        mutate(requestId, "resubmission", ANALYST_A, "{\"expectedVersion\":3}")
                .andExpect(status().isOk()).andExpect(jsonPath("$.status").value("RESUBMISSION_REQUESTED"));

        byte[] secondPdf = "%PDF-1.7\ncorrected document".getBytes(StandardCharsets.UTF_8);
        Upload uploadTwo = createAndStoreUpload(requestId, CLIENT_A, "identity-v2.pdf", secondPdf);
        complete(uploadTwo.id(), CLIENT_A).andExpect(status().isOk())
                .andExpect(jsonPath("$.versionCount").value(2)).andExpect(jsonPath("$.version").value(5));
        mutate(requestId, "review", ANALYST_A, "{\"expectedVersion\":5}").andExpect(status().isOk());
        mutate(requestId, "approval", ANALYST_A, "{\"expectedVersion\":6}")
                .andExpect(status().isOk()).andExpect(jsonPath("$.status").value("APPROVED"));

        JsonNode listed = list(CLIENT_A).andExpect(status().isOk()).andReturnNode(json);
        JsonNode versions = findRequest(listed, requestId).get("versions");
        assertThat(versions).hasSize(2);
        assertThat(versions.get(0).get("id").asText()).isNotEqualTo(versions.get(1).get("id").asText());
        UUID versionId = UUID.fromString(versions.get(1).get("id").asText());
        String downloadBody = mockMvc.perform(authenticated(post(
                        "/api/organizations/{organizationId}/document-versions/{versionId}/downloads",
                        ORGANIZATION_A, versionId), CLIENT_A, ORGANIZATION_A))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
        JsonNode grant = json.readTree(downloadBody);
        String downloadUrl = grant.get("downloadUrl").asText();
        mockMvc.perform(get(downloadUrl)).andExpect(status().isOk()).andExpect(result ->
                assertThat(result.getResponse().getContentAsByteArray()).isEqualTo(secondPdf));
        mockMvc.perform(get(downloadUrl)).andExpect(status().isForbidden());
    }

    @Test
    void shouldEnforceParticipantTenantFileValidationAndIdempotentCleanup() throws Exception {
        submit().andExpect(status().isOk());
        JsonNode request = firstRequest();
        UUID requestId = UUID.fromString(request.get("id").asText());
        list(BROKER_A).andExpect(status().isOk());
        list(THIRD_PARTY).andExpect(status().isForbidden());
        mockMvc.perform(authenticated(get("/api/organizations/{organizationId}/processes/{processId}/documents",
                        ORGANIZATION_B, PROCESS), MANAGER_B, ORGANIZATION_B)).andExpect(status().isNotFound());
        createUpload(requestId, CLIENT_A, "malware.exe", "application/pdf", 10)
                .andExpect(status().isUnprocessableEntity());

        Upload orphan = createAndStoreUpload(requestId, CLIENT_A, "orphan.pdf", PDF);
        jdbc.update("UPDATE document_upload_intents SET expires_at = ? WHERE id = ?", time().minusHours(1), orphan.id());
        assertThat(documents.cleanupExpired(100)).isEqualTo(new ManageDocumentsUseCase.CleanupResult(1, 1));
        assertThat(documents.cleanupExpired(100)).isEqualTo(new ManageDocumentsUseCase.CleanupResult(0, 0));
        assertThat(jdbc.queryForObject("SELECT status FROM document_upload_intents WHERE id = ?",
                String.class, orphan.id())).isEqualTo("EXPIRED");
    }

    private org.springframework.test.web.servlet.ResultActions submit() throws Exception {
        return mockMvc.perform(authenticated(post(
                        "/api/organizations/{organizationId}/processes/{processId}/submission",
                        ORGANIZATION_A, PROCESS), MANAGER_A, ORGANIZATION_A)
                .contentType(MediaType.APPLICATION_JSON).content("{\"expectedVersion\":0}"));
    }
    private JsonNode firstRequest() throws Exception { return list(MANAGER_A).andReturnNode(json).get(0); }
    private JsonActions list(UUID actor) throws Exception {
        return new JsonActions(mockMvc.perform(authenticated(get(
                "/api/organizations/{organizationId}/processes/{processId}/documents",
                ORGANIZATION_A, PROCESS), actor, ORGANIZATION_A)));
    }
    private org.springframework.test.web.servlet.ResultActions createUpload(UUID requestId, UUID actor,
            String fileName, String contentType, long size) throws Exception {
        return mockMvc.perform(authenticated(post(
                        "/api/organizations/{organizationId}/document-requests/{requestId}/uploads",
                        ORGANIZATION_A, requestId), actor, ORGANIZATION_A).contentType(MediaType.APPLICATION_JSON)
                .content("{\"fileName\":\"" + fileName + "\",\"contentType\":\"" + contentType
                        + "\",\"sizeBytes\":" + size + "}"));
    }
    private Upload createAndStoreUpload(UUID requestId, UUID actor, String fileName, byte[] content) throws Exception {
        String body = createUpload(requestId, actor, fileName, "application/pdf", content.length)
                .andExpect(status().isCreated()).andReturn().getResponse().getContentAsString();
        JsonNode upload = json.readTree(body);
        UUID id = UUID.fromString(upload.get("uploadId").asText());
        String url = upload.get("uploadUrl").asText();
        mockMvc.perform(put(url).contentType(MediaType.APPLICATION_PDF).content(content))
                .andExpect(status().isNoContent());
        return new Upload(id, url);
    }
    private org.springframework.test.web.servlet.ResultActions complete(UUID uploadId, UUID actor) throws Exception {
        return mockMvc.perform(authenticated(post(
                        "/api/organizations/{organizationId}/uploads/{uploadId}/complete",
                        ORGANIZATION_A, uploadId), actor, ORGANIZATION_A)
                .contentType(MediaType.APPLICATION_JSON).content("{}"));
    }
    private org.springframework.test.web.servlet.ResultActions mutate(UUID requestId, String action,
            UUID actor, String body) throws Exception {
        return mockMvc.perform(authenticated(patch(
                        "/api/organizations/{organizationId}/document-requests/{requestId}/" + action,
                        ORGANIZATION_A, requestId), actor, ORGANIZATION_A)
                .contentType(MediaType.APPLICATION_JSON).content(body));
    }
    private JsonNode findRequest(JsonNode list, UUID requestId) {
        for (JsonNode item : list) {
            if (item.get("id").asText().equals(requestId.toString())) {
                return item;
            }
        }
        throw new AssertionError("request not found");
    }
    private void assertDraftWithoutGeneratedChecklistOrAudit() {
        assertThat(jdbc.queryForObject("SELECT status FROM financing_processes WHERE id = ?", String.class, PROCESS))
                .isEqualTo("DRAFT");
        assertThat(jdbc.queryForObject("SELECT count(*) FROM document_requests WHERE process_id = ?",
                Integer.class, PROCESS)).isZero();
        assertThat(jdbc.queryForObject("SELECT count(*) FROM financing_process_audit WHERE process_id = ?",
                Integer.class, PROCESS)).isZero();
    }
    private void seedWorkflowModelAndExistingJourney() {
        UUID model = UUID.randomUUID();
        jdbc.update("INSERT INTO workflow_models (id, organization_id, version, name, active, created_at) "
                + "VALUES (?, ?, 1, 'existing', true, ?)", model, ORGANIZATION_A, time());
        jdbc.update("INSERT INTO workflow_stage_definitions (model_id, organization_id, stage_code, stage_name, "
                + "position) VALUES (?, ?, 'INITIAL_REVIEW', 'Initial', 1)", model, ORGANIZATION_A);
        jdbc.update("INSERT INTO workflow_journeys (id, organization_id, process_id, workflow_model_id, "
                + "workflow_version, version, initialized_at, updated_at) VALUES (?, ?, ?, ?, 1, 0, ?, ?)",
                UUID.randomUUID(), ORGANIZATION_A, PROCESS, model, time(), time());
    }
    private void seedConflictingChecklistRequest() {
        UUID type = UUID.randomUUID();
        UUID template = UUID.randomUUID();
        jdbc.update("INSERT INTO document_types (id, organization_id, code, name, allowed_extensions, "
                + "allowed_content_types, maximum_bytes, validity_required, created_at) "
                + "VALUES (?, ?, 'IDENTITY', 'Identity', 'pdf', 'application/pdf', 100000, false, ?)",
                type, ORGANIZATION_A, time());
        jdbc.update("INSERT INTO document_checklist_templates (id, organization_id, version, name, active, created_at) "
                + "VALUES (?, ?, 1, 'Initial', true, ?)", template, ORGANIZATION_A, time());
        jdbc.update("INSERT INTO document_checklist_template_items (template_id, organization_id, "
                + "document_type_id, position, required) VALUES (?, ?, ?, 1, true)", template, ORGANIZATION_A, type);
        jdbc.update("INSERT INTO document_requests (id, organization_id, process_id, document_type_id, recipient_id, "
                + "requested_by, status, version, requested_at, updated_at) VALUES (?, ?, ?, ?, ?, ?, 'REQUESTED', "
                + "0, ?, ?)", UUID.randomUUID(), ORGANIZATION_A, PROCESS, type, CLIENT_A, MANAGER_A, time(), time());
    }
    private void seedDraft() {
        jdbc.update("INSERT INTO financing_processes (id, organization_id, process_number, origin, status, "
                + "author_user_id, broker_id, responsible_user_id, main_client_id, priority, version, created_at, "
                + "updated_at) VALUES (?, ?, 'FP-000001', 'BROKER', 'DRAFT', ?, ?, ?, ?, 'NORMAL', 0, ?, ?)",
                PROCESS, ORGANIZATION_A, MANAGER_A, BROKER_A, ANALYST_A, CLIENT_A, time(), time());
        jdbc.update("INSERT INTO financing_process_participants (organization_id, process_id, participant_type, "
                + "participant_id) VALUES (?, ?, 'CLIENT', ?), (?, ?, 'BROKER', ?)", ORGANIZATION_A, PROCESS,
                CLIENT_A, ORGANIZATION_A, PROCESS, BROKER_A);
        jdbc.update("INSERT INTO financing_process_property_history (organization_id, process_id, sequence, "
                + "address_line, city, state, postal_code, associated_by, associated_at) "
                + "VALUES (?, ?, 1, 'Rua A, 10', 'Fortaleza', 'CE', '60000-000', ?, ?)",
                ORGANIZATION_A, PROCESS, MANAGER_A, time());
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
    private OffsetDateTime time() { return OffsetDateTime.ofInstant(Instant.EPOCH, ZoneOffset.UTC); }
    private record Upload(UUID id, String url) {}
    private record JsonActions(org.springframework.test.web.servlet.ResultActions delegate) {
        JsonActions andExpect(org.springframework.test.web.servlet.ResultMatcher matcher) throws Exception {
            delegate.andExpect(matcher);
            return this;
        }
        JsonNode andReturnNode(ObjectMapper mapper) throws Exception {
            return mapper.readTree(delegate.andExpect(status().isOk()).andReturn().getResponse().getContentAsString());
        }
    }
}
