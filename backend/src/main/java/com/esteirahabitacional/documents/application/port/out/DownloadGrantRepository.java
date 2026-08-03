package com.esteirahabitacional.documents.application.port.out;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

public interface DownloadGrantRepository {
    void insert(Grant grant);
    Optional<Grant> find(UUID grantId);
    void markUsed(UUID grantId, Instant usedAt);

    record Grant(UUID id, UUID organizationId, UUID versionId, String objectKey,
            String fileName, String contentType, String tokenHash, Instant expiresAt, Instant usedAt) {}
}
