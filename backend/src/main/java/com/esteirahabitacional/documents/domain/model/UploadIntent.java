package com.esteirahabitacional.documents.domain.model;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

public final class UploadIntent {
    private final UUID id;
    private final UUID organizationId;
    private final UUID requestId;
    private final UUID senderId;
    private final String objectKey;
    private final String fileName;
    private final String declaredContentType;
    private final long declaredSize;
    private final String tokenHash;
    private final Instant expiresAt;
    private UploadStatus status;
    private Instant updatedAt;

    private UploadIntent(UUID id, UUID organizationId, UUID requestId, UUID senderId,
            String objectKey, String fileName, String declaredContentType, long declaredSize,
            String tokenHash, UploadStatus status, Instant expiresAt, Instant updatedAt) {
        this.id = Objects.requireNonNull(id, "id is required");
        this.organizationId = Objects.requireNonNull(organizationId, "organizationId is required");
        this.requestId = Objects.requireNonNull(requestId, "requestId is required");
        this.senderId = Objects.requireNonNull(senderId, "senderId is required");
        this.objectKey = required(objectKey, "objectKey");
        this.fileName = required(fileName, "fileName");
        this.declaredContentType = required(declaredContentType, "declaredContentType");
        if (declaredSize < 1) {
            throw new IllegalArgumentException("declaredSize must be positive");
        }
        this.declaredSize = declaredSize;
        this.tokenHash = required(tokenHash, "tokenHash");
        this.status = Objects.requireNonNull(status, "status is required");
        this.expiresAt = Objects.requireNonNull(expiresAt, "expiresAt is required");
        this.updatedAt = Objects.requireNonNull(updatedAt, "updatedAt is required");
    }

    public static UploadIntent pending(UUID id, UUID organizationId, UUID requestId, UUID senderId,
            String objectKey, String fileName, String contentType, long size, String tokenHash,
            Instant expiresAt, Instant occurredAt) {
        return new UploadIntent(id, organizationId, requestId, senderId, objectKey, fileName,
                contentType, size, tokenHash, UploadStatus.PENDING, expiresAt, occurredAt);
    }
    public static UploadIntent restore(UUID id, UUID organizationId, UUID requestId, UUID senderId,
            String objectKey, String fileName, String contentType, long size, String tokenHash,
            UploadStatus status, Instant expiresAt, Instant updatedAt) {
        return new UploadIntent(id, organizationId, requestId, senderId, objectKey, fileName,
                contentType, size, tokenHash, status, expiresAt, updatedAt);
    }
    public void stored(Instant occurredAt) {
        if (status != UploadStatus.PENDING) {
            throw new IllegalStateException("Upload is not pending");
        }
        if (!occurredAt.isBefore(expiresAt)) {
            status = UploadStatus.EXPIRED;
            throw new IllegalStateException("Upload intent has expired");
        }
        status = UploadStatus.STORED;
        updatedAt = occurredAt;
    }
    public void complete(Instant occurredAt) {
        if (status == UploadStatus.COMPLETED) {
            return;
        }
        if (status != UploadStatus.STORED) {
            throw new IllegalStateException("Upload was not stored");
        }
        status = UploadStatus.COMPLETED;
        updatedAt = occurredAt;
    }
    public boolean expire(Instant occurredAt) {
        if ((status == UploadStatus.PENDING || status == UploadStatus.STORED)
                && !occurredAt.isBefore(expiresAt)) {
            status = UploadStatus.EXPIRED;
            updatedAt = occurredAt;
            return true;
        }
        return false;
    }
    public void abort(Instant occurredAt) {
        if (status != UploadStatus.COMPLETED) {
            status = UploadStatus.ABORTED;
            updatedAt = occurredAt;
        }
    }
    private static String required(String value, String field) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(field + " is required");
        }
        return value.trim();
    }
    public UUID id() { return id; }
    public UUID organizationId() { return organizationId; }
    public UUID requestId() { return requestId; }
    public UUID senderId() { return senderId; }
    public String objectKey() { return objectKey; }
    public String fileName() { return fileName; }
    public String declaredContentType() { return declaredContentType; }
    public long declaredSize() { return declaredSize; }
    public String tokenHash() { return tokenHash; }
    public UploadStatus status() { return status; }
    public Instant expiresAt() { return expiresAt; }
    public Instant updatedAt() { return updatedAt; }
}
