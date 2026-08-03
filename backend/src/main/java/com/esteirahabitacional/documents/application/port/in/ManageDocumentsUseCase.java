package com.esteirahabitacional.documents.application.port.in;

import java.time.LocalDate;
import java.util.UUID;

public interface ManageDocumentsUseCase {
    RequestResult request(RequestCommand command);
    UploadResult createUpload(UploadCommand command);
    void storeUpload(StoreUploadCommand command);
    RequestResult completeUpload(CompleteUploadCommand command);
    RequestResult markUnderReview(MutationCommand command);
    RequestResult approve(MutationCommand command);
    RequestResult reject(RejectCommand command);
    RequestResult requestResubmission(MutationCommand command);
    DownloadResult createDownload(DownloadCommand command);
    DownloadContent download(UUID grantId, String token);
    CleanupResult cleanupExpired(int limit);

    record RequestCommand(UUID organizationId, UUID processId, UUID documentTypeId, UUID recipientId) {}
    record UploadCommand(UUID organizationId, UUID requestId, String fileName,
            String contentType, long sizeBytes) {}
    record StoreUploadCommand(UUID uploadId, String token, String contentType, byte[] content) {}
    record CompleteUploadCommand(UUID organizationId, UUID uploadId, LocalDate validUntil) {}
    record MutationCommand(UUID organizationId, UUID requestId, long expectedVersion) {}
    record RejectCommand(UUID organizationId, UUID requestId, String reason, long expectedVersion) {}
    record DownloadCommand(UUID organizationId, UUID versionId) {}
    record RequestResult(UUID requestId, String status, int versionCount, long version) {}
    record UploadResult(UUID uploadId, String uploadUrl, java.time.Instant expiresAt) {}
    record DownloadResult(UUID grantId, String downloadUrl, java.time.Instant expiresAt) {}
    record DownloadContent(String fileName, String contentType, byte[] content) {}
    record CleanupResult(int processed, int objectsRemoved) {}
}
