package com.esteirahabitacional.financingprocess;

import java.util.UUID;

public interface FinancingProcessWorkflowLookup {
    Reference find(UUID organizationId, UUID processId);

    enum Lifecycle {
        DRAFT,
        ACTIVE
    }

    record Reference(UUID processId, UUID organizationId, Lifecycle lifecycle) {}
}
