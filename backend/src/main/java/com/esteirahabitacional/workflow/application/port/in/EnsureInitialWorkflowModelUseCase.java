package com.esteirahabitacional.workflow.application.port.in;

import java.util.UUID;

public interface EnsureInitialWorkflowModelUseCase {
    Result execute(UUID organizationId);
    record Result(UUID modelId, int version, int stageCount) {}
}
