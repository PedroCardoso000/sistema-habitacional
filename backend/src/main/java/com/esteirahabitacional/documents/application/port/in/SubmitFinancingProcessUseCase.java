package com.esteirahabitacional.documents.application.port.in;

import java.util.UUID;

public interface SubmitFinancingProcessUseCase {
    Result submit(Command command);
    record Command(UUID organizationId, UUID processId, long expectedVersion) {}
    record Result(UUID processId, String processStatus, String currentStageCode,
            int checklistSize, long processVersion) {}
}
