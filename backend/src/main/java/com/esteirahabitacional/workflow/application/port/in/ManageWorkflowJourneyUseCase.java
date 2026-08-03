package com.esteirahabitacional.workflow.application.port.in;

import java.time.Instant;
import java.util.Set;
import java.util.UUID;

public interface ManageWorkflowJourneyUseCase {
    Result advance(AdvanceCommand command);
    Result returnStage(MoveCommand command);
    Result moveWithException(MoveCommand command);
    Result block(BlockCommand command);
    Result unblock(BlockCommand command);
    Result defineNextAction(NextActionCommand command);

    record AdvanceCommand(UUID organizationId, UUID processId, Set<String> satisfiedCriteria,
                          long expectedVersion) {}
    record MoveCommand(UUID organizationId, UUID processId, String targetStageCode,
                       String justification, long expectedVersion) {}
    record BlockCommand(UUID organizationId, UUID processId, String justification,
                        long expectedVersion) {}
    record NextActionCommand(UUID organizationId, UUID processId, String description,
                             UUID responsibleUserId, Instant dueAt, long expectedVersion) {}
    record Result(UUID journeyId, String currentStageCode, long version) {}
}
