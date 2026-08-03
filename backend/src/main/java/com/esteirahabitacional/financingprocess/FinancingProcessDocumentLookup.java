package com.esteirahabitacional.financingprocess;

import java.util.Set;
import java.util.UUID;

public interface FinancingProcessDocumentLookup {
    Reference find(UUID organizationId, UUID processId);

    record Reference(UUID processId, UUID organizationId, Lifecycle lifecycle,
            UUID responsibleUserId, UUID mainClientId, UUID brokerId,
            Set<UUID> clientParticipantIds, Set<UUID> brokerParticipantIds,
            long version) {
        public Reference {
            clientParticipantIds = Set.copyOf(clientParticipantIds);
            brokerParticipantIds = Set.copyOf(brokerParticipantIds);
        }

        public boolean isClient(UUID userId) { return clientParticipantIds.contains(userId); }
        public boolean isBroker(UUID userId) { return brokerParticipantIds.contains(userId); }
        public boolean isParticipant(UUID participantId) {
            return isClient(participantId) || isBroker(participantId);
        }
    }

    enum Lifecycle { DRAFT, ACTIVE }
}
