package com.esteirahabitacional.documents.domain.model;

import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public final class DocumentRequest {
    private final UUID id;
    private final UUID organizationId;
    private final UUID processId;
    private final UUID documentTypeId;
    private final UUID recipientId;
    private final UUID requestedBy;
    private final Instant requestedAt;
    private final List<DocumentVersion> versions;
    private DocumentStatus status;
    private String rejectionReason;
    private long version;
    private Instant updatedAt;

    private DocumentRequest(UUID id, UUID organizationId, UUID processId, UUID documentTypeId,
            UUID recipientId, UUID requestedBy, DocumentStatus status, String rejectionReason,
            List<DocumentVersion> versions, long version, Instant requestedAt, Instant updatedAt) {
        this.id = Objects.requireNonNull(id, "id is required");
        this.organizationId = Objects.requireNonNull(organizationId, "organizationId is required");
        this.processId = Objects.requireNonNull(processId, "processId is required");
        this.documentTypeId = Objects.requireNonNull(documentTypeId, "documentTypeId is required");
        this.recipientId = Objects.requireNonNull(recipientId, "recipientId is required");
        this.requestedBy = Objects.requireNonNull(requestedBy, "requestedBy is required");
        this.status = Objects.requireNonNull(status, "status is required");
        this.rejectionReason = rejectionReason;
        this.versions = new ArrayList<>(versions);
        this.version = version;
        this.requestedAt = Objects.requireNonNull(requestedAt, "requestedAt is required");
        this.updatedAt = Objects.requireNonNull(updatedAt, "updatedAt is required");
    }

    public static DocumentRequest request(UUID id, UUID organizationId, UUID processId,
            UUID documentTypeId, UUID recipientId, UUID requestedBy, Instant occurredAt) {
        return new DocumentRequest(id, organizationId, processId, documentTypeId, recipientId,
                requestedBy, DocumentStatus.REQUESTED, null, List.of(), 0, occurredAt, occurredAt);
    }

    public static DocumentRequest restore(UUID id, UUID organizationId, UUID processId,
            UUID documentTypeId, UUID recipientId, UUID requestedBy, DocumentStatus status,
            String rejectionReason, List<DocumentVersion> versions, long version,
            Instant requestedAt, Instant updatedAt) {
        return new DocumentRequest(id, organizationId, processId, documentTypeId, recipientId,
                requestedBy, status, rejectionReason, versions, version, requestedAt, updatedAt);
    }

    public void submitVersion(UUID versionId, UUID uploadId, String storageKey, String fileName,
            String contentType, long size, String checksum, UUID senderId, LocalDate validUntil,
            Instant occurredAt) {
        if (status != DocumentStatus.REQUESTED && status != DocumentStatus.RESUBMISSION_REQUESTED
                && status != DocumentStatus.REJECTED) {
            throw new IllegalStateException("Document is not accepting a new version");
        }
        versions.add(new DocumentVersion(versionId, versions.size() + 1, uploadId, storageKey,
                fileName, contentType, size, checksum, senderId, occurredAt, validUntil));
        status = DocumentStatus.SUBMITTED;
        rejectionReason = null;
        updatedAt = occurredAt;
    }

    public void markUnderReview(Instant occurredAt) {
        requireStatus(DocumentStatus.SUBMITTED);
        status = DocumentStatus.UNDER_REVIEW;
        updatedAt = occurredAt;
    }
    public void approve(Instant occurredAt) {
        requireStatus(DocumentStatus.UNDER_REVIEW);
        status = DocumentStatus.APPROVED;
        updatedAt = occurredAt;
    }
    public void reject(String reason, Instant occurredAt) {
        requireStatus(DocumentStatus.UNDER_REVIEW);
        if (reason == null || reason.isBlank()) {
            throw new IllegalArgumentException("Rejection reason is required");
        }
        rejectionReason = reason.trim();
        status = DocumentStatus.REJECTED;
        updatedAt = occurredAt;
    }
    public void requestResubmission(Instant occurredAt) {
        requireStatus(DocumentStatus.REJECTED);
        status = DocumentStatus.RESUBMISSION_REQUESTED;
        updatedAt = occurredAt;
    }

    private void requireStatus(DocumentStatus expected) {
        if (status != expected) {
            throw new IllegalStateException("Document status does not allow this operation");
        }
    }

    public UUID id() { return id; }
    public UUID organizationId() { return organizationId; }
    public UUID processId() { return processId; }
    public UUID documentTypeId() { return documentTypeId; }
    public UUID recipientId() { return recipientId; }
    public UUID requestedBy() { return requestedBy; }
    public DocumentStatus status() { return status; }
    public String rejectionReason() { return rejectionReason; }
    public List<DocumentVersion> versions() { return List.copyOf(versions); }
    public long version() { return version; }
    public Instant requestedAt() { return requestedAt; }
    public Instant updatedAt() { return updatedAt; }
    public boolean acceptsNewVersion() {
        return status == DocumentStatus.REQUESTED || status == DocumentStatus.REJECTED
                || status == DocumentStatus.RESUBMISSION_REQUESTED;
    }
    public void persistedAtVersion(long persistedVersion) { version = persistedVersion; }
}
