package com.esteirahabitacional.documents.application.port.in;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface QueryDocumentsUseCase {
    List<Request> list(UUID organizationId, UUID processId);
    record Request(UUID id, UUID documentTypeId, UUID recipientId, String status,
            String rejectionReason, List<Version> versions, long version, Instant requestedAt) {}
    record Version(UUID id, int number, String fileName, String contentType, long sizeBytes,
            UUID submittedBy, Instant submittedAt, LocalDate validUntil) {}
}
