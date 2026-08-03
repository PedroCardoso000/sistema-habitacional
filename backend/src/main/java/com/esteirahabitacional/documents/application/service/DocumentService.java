package com.esteirahabitacional.documents.application.service;

import com.esteirahabitacional.documents.application.port.in.ManageDocumentsUseCase;
import com.esteirahabitacional.documents.application.port.in.QueryDocumentsUseCase;
import com.esteirahabitacional.documents.application.port.out.DocumentAudit;
import com.esteirahabitacional.documents.application.port.out.DocumentCatalogRepository;
import com.esteirahabitacional.documents.application.port.out.DocumentRequestRepository;
import com.esteirahabitacional.documents.application.port.out.DownloadGrantRepository;
import com.esteirahabitacional.documents.application.port.out.PrivateDocumentStorage;
import com.esteirahabitacional.documents.application.port.out.UploadIntentRepository;
import com.esteirahabitacional.documents.domain.model.ChecklistTemplate;
import com.esteirahabitacional.documents.domain.model.DocumentRequest;
import com.esteirahabitacional.documents.domain.model.DocumentType;
import com.esteirahabitacional.documents.domain.model.DocumentVersion;
import com.esteirahabitacional.documents.domain.model.UploadIntent;
import com.esteirahabitacional.documents.domain.model.UploadStatus;
import com.esteirahabitacional.documents.domain.event.DocumentApproved;
import com.esteirahabitacional.documents.domain.event.DocumentRejected;
import com.esteirahabitacional.documents.domain.event.DocumentRequested;
import com.esteirahabitacional.documents.domain.event.DocumentSubmitted;
import com.esteirahabitacional.financingprocess.FinancingProcessDocumentLookup;
import com.esteirahabitacional.identityaccess.AuthorizeOrganizationUseCase;
import com.esteirahabitacional.identityaccess.CurrentActorContextUseCase;
import com.esteirahabitacional.shared.CurrentTimeProvider;
import com.esteirahabitacional.shared.IdentifierGenerator;
import com.esteirahabitacional.shared.DomainEventPublisher;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import java.time.Instant;
import java.util.HexFormat;
import java.util.List;
import java.util.UUID;

public final class DocumentService {
    private static final Duration UPLOAD_TTL = Duration.ofMinutes(15);
    private static final Duration DOWNLOAD_TTL = Duration.ofMinutes(5);
    private final DocumentAccessService access;
    private final DocumentCatalogRepository catalog;
    private final DocumentRequestRepository requests;
    private final UploadIntentRepository uploads;
    private final DownloadGrantRepository downloads;
    private final PrivateDocumentStorage storage;
    private final DocumentAudit audit;
    private final DomainEventPublisher events;
    private final IdentifierGenerator identifiers;
    private final CurrentTimeProvider time;

    public DocumentService(CurrentActorContextUseCase currentActor,
            AuthorizeOrganizationUseCase authorization, FinancingProcessDocumentLookup processes,
            DocumentCatalogRepository catalog, DocumentRequestRepository requests,
            UploadIntentRepository uploads, DownloadGrantRepository downloads,
            PrivateDocumentStorage storage, DocumentAudit audit,
            DomainEventPublisher events, IdentifierGenerator identifiers, CurrentTimeProvider time) {
        this.access = new DocumentAccessService(currentActor, authorization, processes);
        this.catalog = catalog;
        this.requests = requests;
        this.uploads = uploads;
        this.downloads = downloads;
        this.storage = storage;
        this.audit = audit;
        this.events = events;
        this.identifiers = identifiers;
        this.time = time;
    }

    public List<DocumentRequest> generateChecklist(UUID organizationId, UUID processId,
            UUID recipientId, UUID actorId) {
        Instant now = time.now();
        ChecklistTemplate template = catalog.ensureInitial(organizationId, actorId, now);
        List<DocumentRequest> generated = template.items().stream().map(item -> requests.insert(DocumentRequest.request(
                identifiers.generate(), organizationId, processId, item.documentTypeId(), recipientId,
                actorId, now))).toList();
        audit.record(organizationId, processId, actorId, "CHECKLIST_GENERATED", now);
        events.publish(generated.stream().map(request -> new DocumentRequested(organizationId, processId,
                request.id(), recipientId, actorId, now)).toList());
        return generated;
    }

    public ManageDocumentsUseCase.RequestResult request(ManageDocumentsUseCase.RequestCommand command) {
        var actor = access.requireManage(command.organizationId(), command.processId());
        if (!actor.process().isParticipant(command.recipientId())) {
            throw DocumentExceptions.invalid("Document recipient must be linked to the process");
        }
        catalog.findType(command.organizationId(), command.documentTypeId())
                .orElseThrow(DocumentExceptions::notFound);
        DocumentRequest request = DocumentRequest.request(identifiers.generate(), command.organizationId(),
                command.processId(), command.documentTypeId(), command.recipientId(), actor.actorId(), time.now());
        request = requests.insert(request);
        audit.record(command.organizationId(), command.processId(), actor.actorId(), "DOCUMENT_REQUESTED", time.now());
        events.publish(List.of(new DocumentRequested(command.organizationId(), command.processId(), request.id(),
                command.recipientId(), actor.actorId(), time.now())));
        return result(request);
    }

    public ManageDocumentsUseCase.UploadResult createUpload(ManageDocumentsUseCase.UploadCommand command) {
        DocumentRequest request = load(command.organizationId(), command.requestId());
        var actor = access.requireUpload(command.organizationId(), request.processId(), request.recipientId());
        if (!request.acceptsNewVersion()) {
            throw DocumentExceptions.invalid("Document is not accepting a new version");
        }
        DocumentType type = catalog.findType(command.organizationId(), request.documentTypeId())
                .orElseThrow(DocumentExceptions::notFound);
        validate(() -> type.validate(command.fileName(), command.contentType(), command.sizeBytes()));
        UUID uploadId = identifiers.generate();
        String token = identifiers.generate().toString();
        Instant now = time.now();
        Instant expiresAt = now.plus(UPLOAD_TTL);
        String key = command.organizationId() + "/" + request.processId() + "/" + uploadId;
        uploads.insert(UploadIntent.pending(uploadId, command.organizationId(), request.id(), actor.actorId(),
                key, command.fileName(), command.contentType(), command.sizeBytes(), hash(token), expiresAt, now));
        audit.record(command.organizationId(), request.processId(), actor.actorId(), "UPLOAD_INTENT_CREATED", now);
        return new ManageDocumentsUseCase.UploadResult(uploadId,
                "/api/private-storage/uploads/" + uploadId + "?token=" + token, expiresAt);
    }

    public void storeUpload(ManageDocumentsUseCase.StoreUploadCommand command) {
        UploadIntent intent = uploads.findById(command.uploadId()).orElseThrow(DocumentExceptions::notFound);
        if (!secureEquals(intent.tokenHash(), hash(command.token()))) {
            throw DocumentExceptions.forbidden();
        }
        if (!intent.declaredContentType().equals(command.contentType())
                || intent.declaredSize() != command.content().length) {
            throw DocumentExceptions.invalid("Uploaded metadata differs from declared metadata");
        }
        PrivateDocumentStorage.StoredObject stored;
        try {
            stored = storage.store(intent.objectKey(), command.content(), command.contentType());
        } catch (IllegalArgumentException exception) {
            throw DocumentExceptions.invalid(exception.getMessage());
        } catch (RuntimeException exception) {
            throw DocumentExceptions.storage();
        }
        try {
            validateStored(intent, stored);
            intent.stored(time.now());
            uploads.update(intent);
        } catch (IllegalArgumentException | IllegalStateException exception) {
            throw DocumentExceptions.invalid(exception.getMessage());
        }
    }

    public ManageDocumentsUseCase.RequestResult completeUpload(
            ManageDocumentsUseCase.CompleteUploadCommand command) {
        UploadIntent intent = uploads.findUpload(command.organizationId(), command.uploadId())
                .orElseThrow(DocumentExceptions::notFound);
        DocumentRequest request = load(command.organizationId(), intent.requestId());
        var actor = access.requireUpload(command.organizationId(), request.processId(), request.recipientId());
        if (!actor.actorId().equals(intent.senderId())) {
            throw DocumentExceptions.forbidden();
        }
        if (intent.status() == UploadStatus.COMPLETED) {
            return result(request);
        }
        PrivateDocumentStorage.StoredObject stored;
        try {
            stored = storage.metadata(intent.objectKey());
        } catch (RuntimeException exception) {
            throw DocumentExceptions.storage();
        }
        try {
            validateStored(intent, stored);
            DocumentType type = catalog.findType(command.organizationId(), request.documentTypeId())
                    .orElseThrow(DocumentExceptions::notFound);
            if (type.validityRequired() && command.validUntil() == null) {
                throw new IllegalArgumentException("Document validity is required");
            }
            request.submitVersion(identifiers.generate(), intent.id(), intent.objectKey(), intent.fileName(),
                    stored.contentType(), stored.sizeBytes(), stored.checksum(), actor.actorId(),
                    command.validUntil(), time.now());
            request = requests.update(request, request.version());
            intent.complete(time.now());
            uploads.update(intent);
        } catch (IllegalArgumentException | IllegalStateException exception) {
            throw DocumentExceptions.invalid(exception.getMessage());
        }
        audit.record(command.organizationId(), request.processId(), actor.actorId(), "DOCUMENT_SUBMITTED", time.now());
        events.publish(List.of(new DocumentSubmitted(command.organizationId(), request.processId(), request.id(),
                actor.actorId(), time.now())));
        return result(request);
    }

    public ManageDocumentsUseCase.RequestResult markUnderReview(ManageDocumentsUseCase.MutationCommand command) {
        return analyze(command.organizationId(), command.requestId(), command.expectedVersion(),
                "DOCUMENT_UNDER_REVIEW", DocumentRequest::markUnderReview);
    }
    public ManageDocumentsUseCase.RequestResult approve(ManageDocumentsUseCase.MutationCommand command) {
        return analyze(command.organizationId(), command.requestId(), command.expectedVersion(),
                "DOCUMENT_APPROVED", DocumentRequest::approve);
    }
    public ManageDocumentsUseCase.RequestResult reject(ManageDocumentsUseCase.RejectCommand command) {
        return analyze(command.organizationId(), command.requestId(), command.expectedVersion(),
                "DOCUMENT_REJECTED", (request, now) -> request.reject(command.reason(), now));
    }
    public ManageDocumentsUseCase.RequestResult requestResubmission(ManageDocumentsUseCase.MutationCommand command) {
        return analyze(command.organizationId(), command.requestId(), command.expectedVersion(),
                "DOCUMENT_RESUBMISSION_REQUESTED", DocumentRequest::requestResubmission);
    }

    public List<QueryDocumentsUseCase.Request> list(UUID organizationId, UUID processId) {
        access.requireView(organizationId, processId);
        return requests.findByProcess(organizationId, processId).stream().map(DocumentService::queryResult).toList();
    }

    public ManageDocumentsUseCase.DownloadResult createDownload(ManageDocumentsUseCase.DownloadCommand command) {
        DocumentRequest request = requests.findByVersion(command.organizationId(), command.versionId())
                .orElseThrow(DocumentExceptions::notFound);
        access.requireView(command.organizationId(), request.processId());
        DocumentVersion version = request.versions().stream().filter(item -> item.id().equals(command.versionId()))
                .findFirst().orElseThrow(DocumentExceptions::notFound);
        UUID grantId = identifiers.generate();
        String token = identifiers.generate().toString();
        Instant expiresAt = time.now().plus(DOWNLOAD_TTL);
        downloads.insert(new DownloadGrantRepository.Grant(grantId, command.organizationId(), version.id(),
                version.storageKey(), version.originalFileName(), version.contentType(), hash(token), expiresAt, null));
        return new ManageDocumentsUseCase.DownloadResult(grantId,
                "/api/private-storage/downloads/" + grantId + "?token=" + token, expiresAt);
    }

    public ManageDocumentsUseCase.DownloadContent download(UUID grantId, String token) {
        DownloadGrantRepository.Grant grant = downloads.find(grantId).orElseThrow(DocumentExceptions::notFound);
        if (grant.usedAt() != null || !time.now().isBefore(grant.expiresAt())
                || !secureEquals(grant.tokenHash(), hash(token))) {
            throw DocumentExceptions.forbidden();
        }
        byte[] content = storage.read(grant.objectKey());
        downloads.markUsed(grantId, time.now());
        return new ManageDocumentsUseCase.DownloadContent(grant.fileName(), grant.contentType(), content);
    }

    public ManageDocumentsUseCase.CleanupResult cleanupExpired(int limit) {
        if (limit < 1 || limit > 500) {
            throw DocumentExceptions.invalid("Cleanup limit must be between 1 and 500");
        }
        int removed = 0;
        List<UploadIntent> candidates = uploads.findCleanupCandidates(time.now(), limit);
        for (UploadIntent intent : candidates) {
            storage.deleteIfExists(intent.objectKey());
            intent.expire(time.now());
            uploads.update(intent);
            removed++;
        }
        return new ManageDocumentsUseCase.CleanupResult(candidates.size(), removed);
    }

    private ManageDocumentsUseCase.RequestResult analyze(UUID organizationId, UUID requestId,
            long expectedVersion, String action, Mutation mutation) {
        DocumentRequest request = load(organizationId, requestId);
        var actor = access.requireManage(organizationId, request.processId());
        if (request.version() != expectedVersion) {
            throw DocumentExceptions.conflict();
        }
        try {
            mutation.apply(request, time.now());
        } catch (IllegalArgumentException | IllegalStateException exception) {
            throw DocumentExceptions.invalid(exception.getMessage());
        }
        request = requests.update(request, expectedVersion);
        audit.record(organizationId, request.processId(), actor.actorId(), action, time.now());
        if ("DOCUMENT_APPROVED".equals(action)) {
            events.publish(List.of(new DocumentApproved(organizationId, request.processId(), request.id(),
                    actor.actorId(), time.now())));
        } else if ("DOCUMENT_REJECTED".equals(action)) {
            events.publish(List.of(new DocumentRejected(organizationId, request.processId(), request.id(),
                    actor.actorId(), time.now())));
        }
        return result(request);
    }
    private DocumentRequest load(UUID organizationId, UUID requestId) {
        return requests.findRequest(organizationId, requestId).orElseThrow(DocumentExceptions::notFound);
    }
    private static void validateStored(UploadIntent intent, PrivateDocumentStorage.StoredObject stored) {
        if (!intent.objectKey().equals(stored.objectKey())
                || !intent.declaredContentType().equals(stored.contentType())
                || intent.declaredSize() != stored.sizeBytes()) {
            throw new IllegalStateException("Storage metadata differs from upload intent");
        }
    }
    private static void validate(Runnable validation) {
        try { validation.run(); } catch (IllegalArgumentException exception) {
            throw DocumentExceptions.invalid(exception.getMessage());
        }
    }
    private static ManageDocumentsUseCase.RequestResult result(DocumentRequest request) {
        return new ManageDocumentsUseCase.RequestResult(request.id(), request.status().name(),
                request.versions().size(), request.version());
    }
    private static QueryDocumentsUseCase.Request queryResult(DocumentRequest request) {
        return new QueryDocumentsUseCase.Request(request.id(), request.documentTypeId(), request.recipientId(),
                request.status().name(), request.rejectionReason(), request.versions().stream()
                        .map(version -> new QueryDocumentsUseCase.Version(version.id(), version.number(),
                                version.originalFileName(), version.contentType(), version.sizeBytes(),
                                version.submittedBy(), version.submittedAt(), version.validUntil())).toList(),
                request.version(), request.requestedAt());
    }
    private static String hash(String value) {
        if (value == null || value.isBlank()) {
            throw DocumentExceptions.forbidden();
        }
        try {
            return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256")
                    .digest(value.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException(exception);
        }
    }
    private static boolean secureEquals(String expected, String actual) {
        return MessageDigest.isEqual(expected.getBytes(StandardCharsets.US_ASCII),
                actual.getBytes(StandardCharsets.US_ASCII));
    }
    private interface Mutation { void apply(DocumentRequest request, Instant now); }
}
