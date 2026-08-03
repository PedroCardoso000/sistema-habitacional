package com.esteirahabitacional.documents.domain.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class UploadIntentTest {
    private static final Instant NOW = Instant.parse("2026-08-03T12:00:00Z");

    @Test
    void shouldStoreAndCompleteIdempotently() {
        UploadIntent intent = intent(NOW.plusSeconds(600));
        intent.stored(NOW);
        intent.complete(NOW);
        intent.complete(NOW.plusSeconds(1));
        assertThat(intent.status()).isEqualTo(UploadStatus.COMPLETED);
    }

    @Test
    void shouldExpirePendingIntentIdempotently() {
        UploadIntent intent = intent(NOW.minusSeconds(1));
        assertThat(intent.expire(NOW)).isTrue();
        assertThat(intent.expire(NOW.plusSeconds(1))).isFalse();
        assertThat(intent.status()).isEqualTo(UploadStatus.EXPIRED);
    }

    @Test
    void shouldRejectStoreAfterExpiry() {
        UploadIntent intent = intent(NOW);
        assertThatThrownBy(() -> intent.stored(NOW)).isInstanceOf(IllegalStateException.class);
        assertThat(intent.status()).isEqualTo(UploadStatus.EXPIRED);
    }

    private UploadIntent intent(Instant expiresAt) {
        return UploadIntent.pending(UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(),
                UUID.randomUUID(), "tenant/process/random", "file.pdf", "application/pdf", 10,
                "hash", expiresAt, NOW.minusSeconds(60));
    }
}
