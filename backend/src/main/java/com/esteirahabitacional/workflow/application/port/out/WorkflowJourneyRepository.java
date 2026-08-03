package com.esteirahabitacional.workflow.application.port.out;

import com.esteirahabitacional.workflow.domain.model.WorkflowJourney;
import java.util.Optional;
import java.util.UUID;

public interface WorkflowJourneyRepository {
    Optional<WorkflowJourney> findByProcess(UUID organizationId, UUID processId);
    WorkflowJourney insert(WorkflowJourney journey);
    WorkflowJourney update(WorkflowJourney journey, long expectedVersion);
}
