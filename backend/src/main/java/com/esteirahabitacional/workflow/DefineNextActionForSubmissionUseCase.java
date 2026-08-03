package com.esteirahabitacional.workflow;

import java.time.Instant;
import java.util.UUID;

public interface DefineNextActionForSubmissionUseCase {
    void define(Command command);
    record Command(UUID organizationId, UUID processId, String description,
            UUID responsibleUserId, Instant dueAt, long expectedWorkflowVersion) {}
}
