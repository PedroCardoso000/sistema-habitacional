package com.esteirahabitacional.documents.adapter.out.persistence;

import com.esteirahabitacional.documents.application.port.out.DocumentCatalogRepository;
import com.esteirahabitacional.documents.application.port.out.DocumentRequestRepository;
import com.esteirahabitacional.documents.application.port.out.DownloadGrantRepository;
import com.esteirahabitacional.documents.application.port.out.UploadIntentRepository;
import com.esteirahabitacional.documents.domain.model.ChecklistTemplate;
import com.esteirahabitacional.documents.domain.model.DocumentRequest;
import com.esteirahabitacional.documents.domain.model.DocumentStatus;
import com.esteirahabitacional.documents.domain.model.DocumentType;
import com.esteirahabitacional.documents.domain.model.DocumentVersion;
import com.esteirahabitacional.documents.domain.model.UploadIntent;
import com.esteirahabitacional.documents.domain.model.UploadStatus;
import com.esteirahabitacional.shared.ApplicationException;
import com.esteirahabitacional.shared.IdentifierGenerator;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import org.springframework.jdbc.core.simple.JdbcClient;

public final class JdbcDocumentRepository implements DocumentCatalogRepository,
        DocumentRequestRepository, UploadIntentRepository, DownloadGrantRepository {
    private static final long DEFAULT_MAXIMUM_BYTES = 10L * 1024 * 1024;
    private final JdbcClient jdbc;
    private final IdentifierGenerator identifiers;

    public JdbcDocumentRepository(JdbcClient jdbc, IdentifierGenerator identifiers) {
        this.jdbc = jdbc;
        this.identifiers = identifiers;
    }

    @Override
    public ChecklistTemplate ensureInitial(UUID organizationId, UUID actorId, Instant occurredAt) {
        Optional<ChecklistTemplate> existing = findActiveTemplate(organizationId);
        if (existing.isPresent()) {
            return existing.get();
        }
        DocumentType identity = insertType(organizationId, "IDENTITY", "Documento de identificação", false, occurredAt);
        DocumentType income = insertType(organizationId, "INCOME_PROOF", "Comprovante de renda", false, occurredAt);
        DocumentType residence = insertType(organizationId, "RESIDENCE_PROOF", "Comprovante de residência", false, occurredAt);
        UUID templateId = identifiers.generate();
        jdbc.sql("INSERT INTO document_checklist_templates "
                        + "(id, organization_id, version, name, active, created_at) "
                        + "VALUES (:id, :organizationId, 1, :name, true, :createdAt)")
                .param("id", templateId).param("organizationId", organizationId)
                .param("name", "Checklist habitacional inicial v1").param("createdAt", time(occurredAt)).update();
        List<DocumentType> types = List.of(identity, income, residence);
        for (int index = 0; index < types.size(); index++) {
            jdbc.sql("INSERT INTO document_checklist_template_items "
                            + "(template_id, organization_id, document_type_id, position, required) "
                            + "VALUES (:templateId, :organizationId, :typeId, :position, true)")
                    .param("templateId", templateId).param("organizationId", organizationId)
                    .param("typeId", types.get(index).id()).param("position", index + 1).update();
        }
        return new ChecklistTemplate(templateId, organizationId, 1, "Checklist habitacional inicial v1",
                types.stream().map(type -> new ChecklistTemplate.Item(type.id(), true)).toList(), occurredAt);
    }

    @Override
    public Optional<ChecklistTemplate> findActiveTemplate(UUID organizationId) {
        return jdbc.sql("SELECT id, version, name, created_at FROM document_checklist_templates "
                        + "WHERE organization_id = :organizationId AND active")
                .param("organizationId", organizationId).query((result, row) -> {
                    UUID id = result.getObject("id", UUID.class);
                    List<ChecklistTemplate.Item> items = jdbc.sql("SELECT document_type_id, required "
                                    + "FROM document_checklist_template_items WHERE organization_id = :organizationId "
                                    + "AND template_id = :templateId ORDER BY position")
                            .param("organizationId", organizationId).param("templateId", id)
                            .query((item, number) -> new ChecklistTemplate.Item(
                                    item.getObject("document_type_id", UUID.class), item.getBoolean("required"))).list();
                    return new ChecklistTemplate(id, organizationId, result.getInt("version"),
                            result.getString("name"), items,
                            result.getObject("created_at", OffsetDateTime.class).toInstant());
                }).optional();
    }

    @Override
    public Optional<DocumentType> findType(UUID organizationId, UUID typeId) {
        return jdbc.sql("SELECT id, organization_id, code, name, allowed_extensions, allowed_content_types, "
                        + "maximum_bytes, validity_required FROM document_types "
                        + "WHERE organization_id = :organizationId AND id = :typeId")
                .param("organizationId", organizationId).param("typeId", typeId)
                .query((result, row) -> mapType(result)).optional();
    }

    @Override
    public DocumentRequest insert(DocumentRequest request) {
        jdbc.sql("INSERT INTO document_requests (id, organization_id, process_id, document_type_id, "
                        + "recipient_id, requested_by, status, version, requested_at, updated_at) VALUES "
                        + "(:id, :organizationId, :processId, :typeId, :recipientId, :requestedBy, "
                        + ":status, 0, :requestedAt, :updatedAt)")
                .param("id", request.id()).param("organizationId", request.organizationId())
                .param("processId", request.processId()).param("typeId", request.documentTypeId())
                .param("recipientId", request.recipientId()).param("requestedBy", request.requestedBy())
                .param("status", request.status().name()).param("requestedAt", time(request.requestedAt()))
                .param("updatedAt", time(request.updatedAt())).update();
        return request;
    }

    @Override
    public DocumentRequest update(DocumentRequest request, long expectedVersion) {
        int changed = jdbc.sql("UPDATE document_requests SET status = :status, rejection_reason = :reason, "
                        + "version = version + 1, updated_at = :updatedAt WHERE organization_id = :organizationId "
                        + "AND id = :id AND version = :expectedVersion")
                .param("status", request.status().name()).param("reason", request.rejectionReason())
                .param("updatedAt", time(request.updatedAt())).param("organizationId", request.organizationId())
                .param("id", request.id()).param("expectedVersion", expectedVersion).update();
        if (changed == 0) {
            throw conflict();
        }
        for (DocumentVersion version : request.versions()) {
            jdbc.sql("INSERT INTO document_versions (id, organization_id, request_id, upload_id, version_number, "
                            + "storage_key, original_file_name, content_type, size_bytes, checksum, submitted_by, "
                            + "submitted_at, valid_until) VALUES (:id, :organizationId, :requestId, :uploadId, "
                            + ":number, :storageKey, :fileName, :contentType, :size, :checksum, :submittedBy, "
                            + ":submittedAt, :validUntil) ON CONFLICT (id) DO NOTHING")
                    .param("id", version.id()).param("organizationId", request.organizationId())
                    .param("requestId", request.id()).param("uploadId", version.uploadId())
                    .param("number", version.number()).param("storageKey", version.storageKey())
                    .param("fileName", version.originalFileName()).param("contentType", version.contentType())
                    .param("size", version.sizeBytes()).param("checksum", version.checksum())
                    .param("submittedBy", version.submittedBy()).param("submittedAt", time(version.submittedAt()))
                    .param("validUntil", version.validUntil()).update();
        }
        request.persistedAtVersion(expectedVersion + 1);
        return request;
    }

    @Override
    public Optional<DocumentRequest> findRequest(UUID organizationId, UUID requestId) {
        return jdbc.sql(baseRequestSelect() + " WHERE organization_id = :organizationId AND id = :requestId")
                .param("organizationId", organizationId).param("requestId", requestId)
                .query((result, row) -> mapRequest(result)).optional();
    }

    @Override
    public List<DocumentRequest> findByProcess(UUID organizationId, UUID processId) {
        return jdbc.sql(baseRequestSelect() + " WHERE organization_id = :organizationId "
                        + "AND process_id = :processId ORDER BY requested_at, id")
                .param("organizationId", organizationId).param("processId", processId)
                .query((result, row) -> mapRequest(result)).list();
    }

    @Override
    public Optional<DocumentRequest> findByVersion(UUID organizationId, UUID versionId) {
        return jdbc.sql(baseRequestSelect() + " WHERE organization_id = :organizationId AND id = "
                        + "(SELECT request_id FROM document_versions WHERE organization_id = :organizationId "
                        + "AND id = :versionId)")
                .param("organizationId", organizationId).param("versionId", versionId)
                .query((result, row) -> mapRequest(result)).optional();
    }

    @Override
    public UploadIntent insert(UploadIntent intent) {
        jdbc.sql("INSERT INTO document_upload_intents (id, organization_id, request_id, sender_id, object_key, "
                        + "file_name, declared_content_type, declared_size, token_hash, status, expires_at, updated_at) "
                        + "VALUES (:id, :organizationId, :requestId, :senderId, :objectKey, :fileName, :contentType, "
                        + ":size, :tokenHash, :status, :expiresAt, :updatedAt)")
                .param("id", intent.id()).param("organizationId", intent.organizationId())
                .param("requestId", intent.requestId()).param("senderId", intent.senderId())
                .param("objectKey", intent.objectKey()).param("fileName", intent.fileName())
                .param("contentType", intent.declaredContentType()).param("size", intent.declaredSize())
                .param("tokenHash", intent.tokenHash()).param("status", intent.status().name())
                .param("expiresAt", time(intent.expiresAt())).param("updatedAt", time(intent.updatedAt())).update();
        return intent;
    }

    @Override
    public void update(UploadIntent intent) {
        jdbc.sql("UPDATE document_upload_intents SET status = :status, updated_at = :updatedAt "
                        + "WHERE organization_id = :organizationId AND id = :id")
                .param("status", intent.status().name()).param("updatedAt", time(intent.updatedAt()))
                .param("organizationId", intent.organizationId()).param("id", intent.id()).update();
    }

    @Override
    public Optional<UploadIntent> findUpload(UUID organizationId, UUID uploadId) {
        return jdbc.sql(baseUploadSelect() + " WHERE organization_id = :organizationId AND id = :uploadId")
                .param("organizationId", organizationId).param("uploadId", uploadId)
                .query((result, row) -> mapUpload(result)).optional();
    }

    @Override
    public Optional<UploadIntent> findById(UUID uploadId) {
        return jdbc.sql(baseUploadSelect() + " WHERE id = :uploadId").param("uploadId", uploadId)
                .query((result, row) -> mapUpload(result)).optional();
    }

    @Override
    public List<UploadIntent> findCleanupCandidates(Instant now, int limit) {
        return jdbc.sql(baseUploadSelect() + " WHERE status IN ('PENDING', 'STORED') "
                        + "AND expires_at <= :now ORDER BY expires_at LIMIT :limit")
                .param("now", time(now)).param("limit", limit)
                .query((result, row) -> mapUpload(result)).list();
    }

    @Override
    public void insert(Grant grant) {
        jdbc.sql("INSERT INTO document_download_grants (id, organization_id, version_id, object_key, "
                        + "file_name, content_type, token_hash, expires_at, used_at) VALUES (:id, :organizationId, "
                        + ":versionId, :objectKey, :fileName, :contentType, :tokenHash, :expiresAt, :usedAt)")
                .param("id", grant.id()).param("organizationId", grant.organizationId())
                .param("versionId", grant.versionId()).param("objectKey", grant.objectKey())
                .param("fileName", grant.fileName()).param("contentType", grant.contentType())
                .param("tokenHash", grant.tokenHash()).param("expiresAt", time(grant.expiresAt()))
                .param("usedAt", grant.usedAt() == null ? null : time(grant.usedAt())).update();
    }

    @Override
    public Optional<Grant> find(UUID grantId) {
        return jdbc.sql("SELECT id, organization_id, version_id, object_key, file_name, content_type, token_hash, "
                        + "expires_at, used_at FROM document_download_grants WHERE id = :id")
                .param("id", grantId).query((result, row) -> new Grant(result.getObject("id", UUID.class),
                        result.getObject("organization_id", UUID.class), result.getObject("version_id", UUID.class),
                        result.getString("object_key"), result.getString("file_name"), result.getString("content_type"),
                        result.getString("token_hash"), result.getObject("expires_at", OffsetDateTime.class).toInstant(),
                        nullableInstant(result, "used_at"))).optional();
    }

    @Override
    public void markUsed(UUID grantId, Instant usedAt) {
        jdbc.sql("UPDATE document_download_grants SET used_at = :usedAt WHERE id = :id AND used_at IS NULL")
                .param("usedAt", time(usedAt)).param("id", grantId).update();
    }

    private DocumentType insertType(UUID organizationId, String code, String name,
            boolean validityRequired, Instant occurredAt) {
        UUID id = identifiers.generate();
        jdbc.sql("INSERT INTO document_types (id, organization_id, code, name, allowed_extensions, "
                        + "allowed_content_types, maximum_bytes, validity_required, created_at) VALUES "
                        + "(:id, :organizationId, :code, :name, 'pdf,jpg,jpeg,png', "
                        + "'application/pdf,image/jpeg,image/png', :maximumBytes, :validityRequired, :createdAt)")
                .param("id", id).param("organizationId", organizationId).param("code", code).param("name", name)
                .param("maximumBytes", DEFAULT_MAXIMUM_BYTES).param("validityRequired", validityRequired)
                .param("createdAt", time(occurredAt)).update();
        return new DocumentType(id, organizationId, code, name, Set.of("pdf", "jpg", "jpeg", "png"),
                Set.of("application/pdf", "image/jpeg", "image/png"), DEFAULT_MAXIMUM_BYTES, validityRequired);
    }

    private DocumentType mapType(ResultSet result) throws SQLException {
        return new DocumentType(result.getObject("id", UUID.class), result.getObject("organization_id", UUID.class),
                result.getString("code"), result.getString("name"), split(result.getString("allowed_extensions")),
                split(result.getString("allowed_content_types")), result.getLong("maximum_bytes"),
                result.getBoolean("validity_required"));
    }

    private DocumentRequest mapRequest(ResultSet result) throws SQLException {
        UUID organizationId = result.getObject("organization_id", UUID.class);
        UUID requestId = result.getObject("id", UUID.class);
        List<DocumentVersion> versions = jdbc.sql("SELECT id, upload_id, version_number, storage_key, "
                        + "original_file_name, content_type, size_bytes, checksum, submitted_by, submitted_at, "
                        + "valid_until FROM document_versions WHERE organization_id = :organizationId "
                        + "AND request_id = :requestId ORDER BY version_number")
                .param("organizationId", organizationId).param("requestId", requestId)
                .query((version, row) -> new DocumentVersion(version.getObject("id", UUID.class),
                        version.getInt("version_number"), version.getObject("upload_id", UUID.class),
                        version.getString("storage_key"), version.getString("original_file_name"),
                        version.getString("content_type"), version.getLong("size_bytes"),
                        version.getString("checksum"), version.getObject("submitted_by", UUID.class),
                        version.getObject("submitted_at", OffsetDateTime.class).toInstant(),
                        version.getObject("valid_until", java.time.LocalDate.class))).list();
        return DocumentRequest.restore(requestId, organizationId, result.getObject("process_id", UUID.class),
                result.getObject("document_type_id", UUID.class), result.getObject("recipient_id", UUID.class),
                result.getObject("requested_by", UUID.class), DocumentStatus.valueOf(result.getString("status")),
                result.getString("rejection_reason"), versions, result.getLong("version"),
                result.getObject("requested_at", OffsetDateTime.class).toInstant(),
                result.getObject("updated_at", OffsetDateTime.class).toInstant());
    }

    private UploadIntent mapUpload(ResultSet result) throws SQLException {
        return UploadIntent.restore(result.getObject("id", UUID.class),
                result.getObject("organization_id", UUID.class), result.getObject("request_id", UUID.class),
                result.getObject("sender_id", UUID.class), result.getString("object_key"),
                result.getString("file_name"), result.getString("declared_content_type"),
                result.getLong("declared_size"), result.getString("token_hash"),
                UploadStatus.valueOf(result.getString("status")),
                result.getObject("expires_at", OffsetDateTime.class).toInstant(),
                result.getObject("updated_at", OffsetDateTime.class).toInstant());
    }

    private String baseRequestSelect() {
        return "SELECT id, organization_id, process_id, document_type_id, recipient_id, requested_by, "
                + "status, rejection_reason, version, requested_at, updated_at FROM document_requests";
    }
    private String baseUploadSelect() {
        return "SELECT id, organization_id, request_id, sender_id, object_key, file_name, declared_content_type, "
                + "declared_size, token_hash, status, expires_at, updated_at FROM document_upload_intents";
    }
    private static Set<String> split(String value) {
        return new LinkedHashSet<>(Arrays.asList(value.split(",")));
    }
    private static OffsetDateTime time(Instant instant) { return OffsetDateTime.ofInstant(instant, ZoneOffset.UTC); }
    private static Instant nullableInstant(ResultSet result, String column) throws SQLException {
        OffsetDateTime value = result.getObject(column, OffsetDateTime.class);
        return value == null ? null : value.toInstant();
    }
    private static ApplicationException conflict() {
        return new ApplicationException(409, "document-version-conflict", "Conflito de versão",
                "O documento foi alterado por outra operação.");
    }
}
