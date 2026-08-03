package com.esteirahabitacional.financingprocess.application.port.in;

import com.esteirahabitacional.financingprocess.domain.model.ParticipantType;
import com.esteirahabitacional.financingprocess.domain.model.ProcessOrigin;
import com.esteirahabitacional.financingprocess.domain.model.ProcessPriority;
import java.util.UUID;

public interface ManageFinancingProcessUseCase {
    Result create(CreateCommand command);
    Result defineMainClient(MainClientCommand command);
    Result associateParticipant(ParticipantCommand command);
    Result associateProperty(PropertyCommand command);
    Result changePriority(PriorityCommand command);

    record CreateCommand(UUID organizationId, ProcessOrigin origin, UUID brokerId, UUID mainClientId) {}
    record MainClientCommand(UUID organizationId, UUID processId, UUID clientId, long expectedVersion) {}
    record ParticipantCommand(UUID organizationId, UUID processId, ParticipantType type,
                              UUID participantId, long expectedVersion) {}
    record PropertyCommand(UUID organizationId, UUID processId, String addressLine, String city,
                           String state, String postalCode, long expectedVersion) {}
    record PriorityCommand(UUID organizationId, UUID processId, ProcessPriority priority,
                           long expectedVersion) {}
    record Result(UUID id, String processNumber, long version) {}
}
