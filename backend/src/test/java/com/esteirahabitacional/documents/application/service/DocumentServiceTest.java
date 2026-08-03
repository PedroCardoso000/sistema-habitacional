package com.esteirahabitacional.documents.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.esteirahabitacional.documents.application.port.in.ManageDocumentsUseCase;
import com.esteirahabitacional.documents.application.port.out.DocumentAudit;
import com.esteirahabitacional.documents.application.port.out.DocumentCatalogRepository;
import com.esteirahabitacional.documents.application.port.out.DocumentRequestRepository;
import com.esteirahabitacional.documents.application.port.out.DownloadGrantRepository;
import com.esteirahabitacional.documents.application.port.out.PrivateDocumentStorage;
import com.esteirahabitacional.documents.application.port.out.UploadIntentRepository;
import com.esteirahabitacional.documents.domain.model.UploadIntent;
import com.esteirahabitacional.financingprocess.FinancingProcessDocumentLookup;
import com.esteirahabitacional.identityaccess.AuthorizeOrganizationUseCase;
import com.esteirahabitacional.identityaccess.CurrentActorContextUseCase;
import com.esteirahabitacional.shared.ApplicationException;
import com.esteirahabitacional.shared.CurrentTimeProvider;
import com.esteirahabitacional.shared.IdentifierGenerator;
import com.esteirahabitacional.shared.DomainEventPublisher;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class DocumentServiceTest {
    @Test
    void shouldTranslateStorageFailureWithoutChangingIntentState() {
        UUID uploadId = UUID.randomUUID();
        byte[] content = "%PDF-1.4".getBytes(java.nio.charset.StandardCharsets.UTF_8);
        UploadIntent intent = UploadIntent.pending(uploadId, UUID.randomUUID(), UUID.randomUUID(),
                UUID.randomUUID(), "tenant/process/random", "file.pdf", "application/pdf", content.length,
                hash("token"), Instant.parse("2026-08-03T13:00:00Z"), Instant.parse("2026-08-03T12:00:00Z"));
        UploadIntentRepository uploads = mock(UploadIntentRepository.class);
        PrivateDocumentStorage storage = mock(PrivateDocumentStorage.class);
        when(uploads.findById(uploadId)).thenReturn(Optional.of(intent));
        when(storage.store(intent.objectKey(), content, "application/pdf"))
                .thenThrow(new IllegalStateException("disk unavailable"));
        DocumentService service = service(uploads, storage);

        assertThatThrownBy(() -> service.storeUpload(new ManageDocumentsUseCase.StoreUploadCommand(
                uploadId, "token", "application/pdf", content)))
                .isInstanceOf(ApplicationException.class)
                .extracting(exception -> ((ApplicationException) exception).status()).isEqualTo(503);
        assertThat(intent.status().name()).isEqualTo("PENDING");
        verify(uploads, never()).update(intent);
    }

    private DocumentService service(UploadIntentRepository uploads, PrivateDocumentStorage storage) {
        return new DocumentService(mock(CurrentActorContextUseCase.class),
                mock(AuthorizeOrganizationUseCase.class), mock(FinancingProcessDocumentLookup.class),
                mock(DocumentCatalogRepository.class), mock(DocumentRequestRepository.class), uploads,
                mock(DownloadGrantRepository.class), storage, mock(DocumentAudit.class),
                mock(DomainEventPublisher.class), mock(IdentifierGenerator.class), mock(CurrentTimeProvider.class));
    }
    private static String hash(String value) {
        try {
            return java.util.HexFormat.of().formatHex(java.security.MessageDigest.getInstance("SHA-256")
                    .digest(value.getBytes(java.nio.charset.StandardCharsets.UTF_8)));
        } catch (java.security.NoSuchAlgorithmException exception) {
            throw new IllegalStateException(exception);
        }
    }
}
