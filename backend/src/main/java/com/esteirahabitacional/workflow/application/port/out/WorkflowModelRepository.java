package com.esteirahabitacional.workflow.application.port.out;

import com.esteirahabitacional.workflow.domain.model.WorkflowModel;
import java.util.Optional;
import java.util.UUID;

public interface WorkflowModelRepository {
    Optional<WorkflowModel> findActive(UUID organizationId);
    WorkflowModel insert(WorkflowModel model);
}
