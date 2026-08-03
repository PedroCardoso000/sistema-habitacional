package com.esteirahabitacional.documents.domain.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class DocumentRequestTest {
    private static final Instant NOW = Instant.parse("2026-08-03T12:00:00Z");

    @Test
    void shouldCompleteApprovalCycle() {
        DocumentRequest request = request();
        request.submitVersion(UUID.randomUUID(), UUID.randomUUID(), "org/process/random.pdf",
                "identity.pdf", "application/pdf", 100, "checksum", UUID.randomUUID(), null, NOW);
        request.markUnderReview(NOW.plusSeconds(1));
        request.approve(NOW.plusSeconds(2));
        assertThat(request.status()).isEqualTo(DocumentStatus.APPROVED);
        assertThat(request.versions()).hasSize(1);
    }

    @Test
    void shouldRequireReasonAndPreservePreviousVersionOnResubmission() {
        DocumentRequest request = request();
        request.submitVersion(UUID.randomUUID(), UUID.randomUUID(), "key/one", "one.pdf",
                "application/pdf", 100, "one", UUID.randomUUID(), null, NOW);
        request.markUnderReview(NOW);
        assertThatThrownBy(() -> request.reject(" ", NOW)).isInstanceOf(IllegalArgumentException.class);
        request.reject("Documento ilegível", NOW);
        request.requestResubmission(NOW);
        request.submitVersion(UUID.randomUUID(), UUID.randomUUID(), "key/two", "two.pdf",
                "application/pdf", 110, "two", UUID.randomUUID(), null, NOW);
        assertThat(request.versions()).extracting(DocumentVersion::number).containsExactly(1, 2);
        assertThat(request.versions()).extracting(DocumentVersion::storageKey).containsExactly("key/one", "key/two");
    }

    @Test
    void shouldRejectSubmissionWhenStatusDoesNotAcceptVersion() {
        DocumentRequest request = request();
        request.submitVersion(UUID.randomUUID(), UUID.randomUUID(), "key/one", "one.pdf",
                "application/pdf", 100, "one", UUID.randomUUID(), null, NOW);
        assertThatThrownBy(() -> request.submitVersion(UUID.randomUUID(), UUID.randomUUID(), "key/two",
                "two.pdf", "application/pdf", 100, "two", UUID.randomUUID(), null, NOW))
                .isInstanceOf(IllegalStateException.class);
    }

    private DocumentRequest request() {
        return DocumentRequest.request(UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(),
                UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(), NOW);
    }
}
