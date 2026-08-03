package com.esteirahabitacional.financingprocess.application.port.in;

import com.esteirahabitacional.financingprocess.domain.model.ParticipantType;
import com.esteirahabitacional.financingprocess.domain.model.ProcessOrigin;
import com.esteirahabitacional.financingprocess.domain.model.ProcessPriority;
import com.esteirahabitacional.financingprocess.domain.model.ProcessStatus;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public interface QueryFinancingProcessUseCase {
    Detail find(UUID organizationId, UUID processId);
    Page list(ListQuery query);

    record ListQuery(UUID organizationId, ProcessOrigin origin, ProcessPriority priority, int page, int size) {}
    record Participant(ParticipantType type, UUID participantId) {}
    record Property(int sequence, String addressLine, String city, String state, String postalCode,
                    UUID associatedBy, Instant associatedAt) {}
    record Detail(UUID id, String processNumber, UUID organizationId, ProcessOrigin origin, ProcessStatus status,
                  UUID authorUserId, UUID brokerId, UUID responsibleUserId, UUID mainClientId,
                  ProcessPriority priority, List<Participant> participants, List<Property> propertyHistory,
                  long version, Instant createdAt, Instant updatedAt) {}
    record Page(List<Detail> items, int page, int size, long total) {}
}
