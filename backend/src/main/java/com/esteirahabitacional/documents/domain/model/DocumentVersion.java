package com.esteirahabitacional.documents.domain.model;

import java.time.Instant;
import java.time.LocalDate;
import java.util.Objects;
import java.util.UUID;

public record DocumentVersion(UUID id, int number, UUID uploadId, String storageKey,
        String originalFileName, String contentType, long sizeBytes, String checksum,
        UUID submittedBy, Instant submittedAt, LocalDate validUntil) {
    public DocumentVersion {
        Objects.requireNonNull(id, "id is required");
        if (number < 1) {
            throw new IllegalArgumentException("version number must be positive");
        }
        Objects.requireNonNull(uploadId, "uploadId is required");
        storageKey = required(storageKey, "storageKey");
        originalFileName = required(originalFileName, "originalFileName");
        contentType = required(contentType, "contentType");
        if (sizeBytes < 1) {
            throw new IllegalArgumentException("sizeBytes must be positive");
        }
        checksum = required(checksum, "checksum");
        Objects.requireNonNull(submittedBy, "submittedBy is required");
        Objects.requireNonNull(submittedAt, "submittedAt is required");
    }
    private static String required(String value, String field) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(field + " is required");
        }
        return value.trim();
    }
}
