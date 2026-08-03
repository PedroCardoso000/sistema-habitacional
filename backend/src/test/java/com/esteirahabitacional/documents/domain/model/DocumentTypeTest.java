package com.esteirahabitacional.documents.domain.model;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class DocumentTypeTest {
    private final DocumentType type = new DocumentType(UUID.randomUUID(), UUID.randomUUID(),
            "IDENTITY", "Identity", Set.of("pdf", "jpg"),
            Set.of("application/pdf", "image/jpeg"), 1024, false);

    @Test void shouldAcceptAllowedFile() {
        assertThatCode(() -> type.validate("identity.PDF", "application/pdf", 1024)).doesNotThrowAnyException();
    }
    @Test void shouldRejectExtensionContentTypeAndSize() {
        assertThatThrownBy(() -> type.validate("file.exe", "application/pdf", 10))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> type.validate("file.pdf", "text/plain", 10))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> type.validate("file.pdf", "application/pdf", 1025))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
