package com.esteirahabitacional.workflow;

import java.util.UUID;

public interface InitializeWorkflowForSubmissionUseCase {
    Result initialize(Command command);

    record Command(UUID organizationId, UUID processId, UUID actorId) {}
    record Result(UUID journeyId, UUID workflowModelId, int workflowVersion, String currentStageCode) {}
}
