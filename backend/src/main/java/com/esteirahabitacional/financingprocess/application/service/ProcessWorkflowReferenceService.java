package com.esteirahabitacional.financingprocess.application.service;

import com.esteirahabitacional.financingprocess.FinancingProcessWorkflowLookup;
import com.esteirahabitacional.financingprocess.application.port.out.FinancingProcessRepository;
import com.esteirahabitacional.financingprocess.domain.model.FinancingProcess;
import java.util.UUID;

public final class ProcessWorkflowReferenceService implements FinancingProcessWorkflowLookup {
    private final FinancingProcessRepository processes;

    public ProcessWorkflowReferenceService(FinancingProcessRepository processes) {
        this.processes = processes;
    }

    @Override
    public Reference find(UUID organizationId, UUID processId) {
        FinancingProcess process = processes.findById(organizationId, processId)
                .orElseThrow(ProcessExceptions::notFound);
        return new Reference(process.id(), process.organizationId(),
                Lifecycle.valueOf(process.status().name()));
    }
}
