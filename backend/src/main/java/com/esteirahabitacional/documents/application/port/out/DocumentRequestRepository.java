package com.esteirahabitacional.documents.application.port.out;

import com.esteirahabitacional.documents.domain.model.DocumentRequest;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface DocumentRequestRepository {
    DocumentRequest insert(DocumentRequest request);
    DocumentRequest update(DocumentRequest request, long expectedVersion);
    Optional<DocumentRequest> findRequest(UUID organizationId, UUID requestId);
    List<DocumentRequest> findByProcess(UUID organizationId, UUID processId);
    Optional<DocumentRequest> findByVersion(UUID organizationId, UUID versionId);
}
