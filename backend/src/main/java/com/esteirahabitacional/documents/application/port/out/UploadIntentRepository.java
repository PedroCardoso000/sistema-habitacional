package com.esteirahabitacional.documents.application.port.out;

import com.esteirahabitacional.documents.domain.model.UploadIntent;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UploadIntentRepository {
    UploadIntent insert(UploadIntent intent);
    void update(UploadIntent intent);
    Optional<UploadIntent> findUpload(UUID organizationId, UUID uploadId);
    Optional<UploadIntent> findById(UUID uploadId);
    List<UploadIntent> findCleanupCandidates(Instant now, int limit);
}
