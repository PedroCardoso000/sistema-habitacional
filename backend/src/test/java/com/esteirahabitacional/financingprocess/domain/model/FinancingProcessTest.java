package com.esteirahabitacional.financingprocess.domain.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Instant;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class FinancingProcessTest {
    private static final UUID ORGANIZATION = UUID.randomUUID();
    private static final UUID ACTOR = UUID.randomUUID();

    @Test
    void shouldCreateDirectDraftWithoutPropertyOrClient() {
        FinancingProcess process = FinancingProcess.draft(UUID.randomUUID(), "FP-000001", ORGANIZATION,
                ProcessOrigin.DIRECT_CLIENT, ACTOR, null, null, Instant.EPOCH);

        assertThat(process.priority()).isEqualTo(ProcessPriority.NORMAL);
        assertThat(process.status()).isEqualTo(ProcessStatus.DRAFT);
        assertThat(process.propertyHistory()).isEmpty();
        assertThat(process.participants()).isEmpty();
        assertThat(process.version()).isZero();
    }

    @Test
    void shouldRequireBrokerOnlyForBrokerOrigin() {
        assertThatThrownBy(() -> FinancingProcess.draft(UUID.randomUUID(), "FP-000001", ORGANIZATION,
                ProcessOrigin.BROKER, ACTOR, null, null, Instant.EPOCH))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("brokerId");
        assertThatThrownBy(() -> FinancingProcess.draft(UUID.randomUUID(), "FP-000001", ORGANIZATION,
                ProcessOrigin.DIRECT_CLIENT, ACTOR, UUID.randomUUID(), null, Instant.EPOCH))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void shouldPreservePropertyHistoryAndRejectUnlinkedBroker() {
        UUID broker = UUID.randomUUID();
        FinancingProcess process = FinancingProcess.draft(UUID.randomUUID(), "FP-000001", ORGANIZATION,
                ProcessOrigin.BROKER, ACTOR, broker, null, Instant.EPOCH);

        process.associateProperty("Rua A, 10", "Fortaleza", "CE", "60000-000", ACTOR, Instant.EPOCH);
        process.associateProperty("Rua B, 20", "Fortaleza", "CE", "60100-000", ACTOR,
                Instant.EPOCH.plusSeconds(1));

        assertThat(process.propertyHistory()).extracting(PropertyAssociation::sequence).containsExactly(1, 2);
        assertThat(process.isLinkedBroker(broker)).isTrue();
        assertThat(process.isLinkedBroker(UUID.randomUUID())).isFalse();
    }

    @Test
    void shouldRejectDraftOperationsAfterProcessBecomesActive() {
        FinancingProcess process = FinancingProcess.restore(UUID.randomUUID(), "FP-000001", ORGANIZATION,
                ProcessOrigin.DIRECT_CLIENT, ProcessStatus.ACTIVE, ACTOR, null, ACTOR, null,
                ProcessPriority.NORMAL, Set.of(), List.of(), 1, Instant.EPOCH, Instant.EPOCH);

        assertThatThrownBy(() -> process.changePriority(ProcessPriority.HIGH, Instant.EPOCH.plusSeconds(1)))
                .isInstanceOf(IllegalStateException.class).hasMessageContaining("draft");
    }

    @Test
    void shouldRequireClientAndPropertyBeforeSubmission() {
        FinancingProcess process = FinancingProcess.draft(UUID.randomUUID(), "FP-000001", ORGANIZATION,
                ProcessOrigin.DIRECT_CLIENT, ACTOR, null, null, Instant.EPOCH);
        assertThatThrownBy(() -> process.activateForSubmission(Instant.EPOCH))
                .isInstanceOf(IllegalStateException.class).hasMessageContaining("client");
        process.defineMainClient(UUID.randomUUID(), Instant.EPOCH);
        assertThatThrownBy(() -> process.activateForSubmission(Instant.EPOCH))
                .isInstanceOf(IllegalStateException.class).hasMessageContaining("Property");
        process.associateProperty("Rua A, 10", "Fortaleza", "CE", "60000-000", ACTOR, Instant.EPOCH);
        process.activateForSubmission(Instant.EPOCH);
        assertThat(process.status()).isEqualTo(ProcessStatus.ACTIVE);
    }
}
