package com.esteirahabitacional.financingprocess.application.service;

import com.esteirahabitacional.financingprocess.FinancingProcessDocumentLookup;
import com.esteirahabitacional.financingprocess.application.port.out.FinancingProcessRepository;
import com.esteirahabitacional.financingprocess.domain.model.FinancingProcess;
import com.esteirahabitacional.financingprocess.domain.model.ParticipantType;
import java.util.UUID;
import java.util.stream.Collectors;

public final class ProcessDocumentReferenceService implements FinancingProcessDocumentLookup {
    private final FinancingProcessRepository processes;

    public ProcessDocumentReferenceService(FinancingProcessRepository processes) {
        this.processes = processes;
    }

    @Override
    public Reference find(UUID organizationId, UUID processId) {
        FinancingProcess process = processes.findById(organizationId, processId)
                .orElseThrow(ProcessExceptions::notFound);
        return new Reference(process.id(), process.organizationId(),
                Lifecycle.valueOf(process.status().name()), process.responsibleUserId(),
                process.mainClientId(), process.brokerId(),
                process.participants().stream().filter(item -> item.type() == ParticipantType.CLIENT)
                        .map(item -> item.participantId()).collect(Collectors.toSet()),
                process.participants().stream().filter(item -> item.type() == ParticipantType.BROKER)
                        .map(item -> item.participantId()).collect(Collectors.toSet()), process.version());
    }
}
