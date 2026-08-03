package com.esteirahabitacional.documents.adapter.in.web;

import com.esteirahabitacional.documents.application.port.in.ManageDocumentsUseCase;
import com.esteirahabitacional.documents.application.port.in.QueryDocumentsUseCase;
import com.esteirahabitacional.documents.application.port.in.SubmitFinancingProcessUseCase;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.net.URI;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
class DocumentsController {
    private final SubmitFinancingProcessUseCase submission;
    private final ManageDocumentsUseCase management;
    private final QueryDocumentsUseCase queries;

    DocumentsController(SubmitFinancingProcessUseCase submission,
            ManageDocumentsUseCase management, QueryDocumentsUseCase queries) {
        this.submission = submission;
        this.management = management;
        this.queries = queries;
    }

    @PostMapping("/organizations/{organizationId}/processes/{processId}/submission")
    SubmissionResponse submit(@PathVariable UUID organizationId, @PathVariable UUID processId,
            @Valid @RequestBody SubmissionRequest request) {
        var result = submission.submit(new SubmitFinancingProcessUseCase.Command(
                organizationId, processId, request.expectedVersion()));
        return new SubmissionResponse(result.processId(), result.processStatus(), result.currentStageCode(),
                result.checklistSize(), result.processVersion());
    }

    @GetMapping("/organizations/{organizationId}/processes/{processId}/documents")
    List<DocumentResponse> list(@PathVariable UUID organizationId, @PathVariable UUID processId) {
        return queries.list(organizationId, processId).stream().map(DocumentResponse::from).toList();
    }

    @PostMapping("/organizations/{organizationId}/processes/{processId}/document-requests")
    ResponseEntity<RequestResponse> request(@PathVariable UUID organizationId, @PathVariable UUID processId,
            @Valid @RequestBody CreateRequest request) {
        var result = management.request(new ManageDocumentsUseCase.RequestCommand(organizationId,
                processId, request.documentTypeId(), request.recipientId()));
        return ResponseEntity.created(URI.create("/api/organizations/" + organizationId
                        + "/document-requests/" + result.requestId())).body(RequestResponse.from(result));
    }

    @PostMapping("/organizations/{organizationId}/document-requests/{requestId}/uploads")
    ResponseEntity<UploadResponse> createUpload(@PathVariable UUID organizationId, @PathVariable UUID requestId,
            @Valid @RequestBody UploadRequest request) {
        var result = management.createUpload(new ManageDocumentsUseCase.UploadCommand(organizationId,
                requestId, request.fileName(), request.contentType(), request.sizeBytes()));
        return ResponseEntity.created(URI.create(result.uploadUrl()))
                .body(new UploadResponse(result.uploadId(), result.uploadUrl(), result.expiresAt()));
    }

    @PutMapping(path = "/private-storage/uploads/{uploadId}", consumes = MediaType.ALL_VALUE)
    ResponseEntity<Void> store(@PathVariable UUID uploadId, @RequestParam String token,
            @RequestHeader(HttpHeaders.CONTENT_TYPE) String contentType, @RequestBody byte[] content) {
        MediaType mediaType = MediaType.parseMediaType(contentType);
        String normalized = new MediaType(mediaType.getType(), mediaType.getSubtype()).toString();
        management.storeUpload(new ManageDocumentsUseCase.StoreUploadCommand(uploadId, token, normalized, content));
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/organizations/{organizationId}/uploads/{uploadId}/complete")
    RequestResponse complete(@PathVariable UUID organizationId, @PathVariable UUID uploadId,
            @Valid @RequestBody CompleteUploadRequest request) {
        return RequestResponse.from(management.completeUpload(new ManageDocumentsUseCase.CompleteUploadCommand(
                organizationId, uploadId, request.validUntil())));
    }

    @PatchMapping("/organizations/{organizationId}/document-requests/{requestId}/review")
    RequestResponse review(@PathVariable UUID organizationId, @PathVariable UUID requestId,
            @Valid @RequestBody VersionRequest request) {
        return RequestResponse.from(management.markUnderReview(new ManageDocumentsUseCase.MutationCommand(
                organizationId, requestId, request.expectedVersion())));
    }

    @PatchMapping("/organizations/{organizationId}/document-requests/{requestId}/approval")
    RequestResponse approve(@PathVariable UUID organizationId, @PathVariable UUID requestId,
            @Valid @RequestBody VersionRequest request) {
        return RequestResponse.from(management.approve(new ManageDocumentsUseCase.MutationCommand(
                organizationId, requestId, request.expectedVersion())));
    }

    @PatchMapping("/organizations/{organizationId}/document-requests/{requestId}/rejection")
    RequestResponse reject(@PathVariable UUID organizationId, @PathVariable UUID requestId,
            @Valid @RequestBody RejectionRequest request) {
        return RequestResponse.from(management.reject(new ManageDocumentsUseCase.RejectCommand(
                organizationId, requestId, request.reason(), request.expectedVersion())));
    }

    @PatchMapping("/organizations/{organizationId}/document-requests/{requestId}/resubmission")
    RequestResponse resubmission(@PathVariable UUID organizationId, @PathVariable UUID requestId,
            @Valid @RequestBody VersionRequest request) {
        return RequestResponse.from(management.requestResubmission(new ManageDocumentsUseCase.MutationCommand(
                organizationId, requestId, request.expectedVersion())));
    }

    @PostMapping("/organizations/{organizationId}/document-versions/{versionId}/downloads")
    DownloadResponse createDownload(@PathVariable UUID organizationId, @PathVariable UUID versionId) {
        var result = management.createDownload(new ManageDocumentsUseCase.DownloadCommand(organizationId, versionId));
        return new DownloadResponse(result.grantId(), result.downloadUrl(), result.expiresAt());
    }

    @GetMapping("/private-storage/downloads/{grantId}")
    ResponseEntity<byte[]> download(@PathVariable UUID grantId, @RequestParam String token) {
        var result = management.download(grantId, token);
        return ResponseEntity.ok().contentType(MediaType.parseMediaType(result.contentType()))
                .header(HttpHeaders.CONTENT_DISPOSITION, ContentDisposition.attachment()
                        .filename(result.fileName()).build().toString()).body(result.content());
    }

    record SubmissionRequest(@Min(0) long expectedVersion) {}
    record SubmissionResponse(UUID processId, String processStatus, String currentStageCode,
            int checklistSize, long processVersion) {}
    record CreateRequest(@NotNull UUID documentTypeId, @NotNull UUID recipientId) {}
    record UploadRequest(@NotBlank String fileName, @NotBlank String contentType, @Positive long sizeBytes) {}
    record CompleteUploadRequest(LocalDate validUntil) {}
    record VersionRequest(@Min(0) long expectedVersion) {}
    record RejectionRequest(@NotBlank String reason, @Min(0) long expectedVersion) {}
    record RequestResponse(UUID requestId, String status, int versionCount, long version) {
        static RequestResponse from(ManageDocumentsUseCase.RequestResult result) {
            return new RequestResponse(result.requestId(), result.status(), result.versionCount(), result.version());
        }
    }
    record UploadResponse(UUID uploadId, String uploadUrl, Instant expiresAt) {}
    record DownloadResponse(UUID grantId, String downloadUrl, Instant expiresAt) {}
    record DocumentResponse(UUID id, UUID documentTypeId, UUID recipientId, String status,
            String rejectionReason, List<VersionResponse> versions, long version, Instant requestedAt) {
        static DocumentResponse from(QueryDocumentsUseCase.Request request) {
            return new DocumentResponse(request.id(), request.documentTypeId(), request.recipientId(),
                    request.status(), request.rejectionReason(), request.versions().stream()
                            .map(VersionResponse::from).toList(), request.version(), request.requestedAt());
        }
    }
    record VersionResponse(UUID id, int number, String fileName, String contentType, long sizeBytes,
            UUID submittedBy, Instant submittedAt, LocalDate validUntil) {
        static VersionResponse from(QueryDocumentsUseCase.Version version) {
            return new VersionResponse(version.id(), version.number(), version.fileName(), version.contentType(),
                    version.sizeBytes(), version.submittedBy(), version.submittedAt(), version.validUntil());
        }
    }
}
