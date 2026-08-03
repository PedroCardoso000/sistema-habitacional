package com.esteirahabitacional.workflow.application.port.in;

import com.esteirahabitacional.workflow.domain.model.StageStatus;
import com.esteirahabitacional.workflow.domain.model.TransitionType;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public interface QueryWorkflowJourneyUseCase {
    Journey find(UUID organizationId, UUID processId);

    record Stage(String code, String name, int position, StageStatus status,
                 Instant startedAt, Instant completedAt) {}
    record Transition(int sequence, TransitionType type, String fromStageCode, String toStageCode,
                      String justification, UUID actorId, Instant occurredAt) {}
    record Action(String description, UUID responsibleUserId, Instant dueAt, Instant definedAt) {}
    record Journey(UUID id, UUID processId, UUID workflowModelId, int workflowVersion,
                   Stage currentStage, List<Stage> stages, List<Transition> transitions,
                   Action nextAction, boolean missingNextAction, long version) {}
}
