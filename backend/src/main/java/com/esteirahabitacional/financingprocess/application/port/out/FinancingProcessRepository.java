package com.esteirahabitacional.financingprocess.application.port.out;

import com.esteirahabitacional.financingprocess.domain.model.FinancingProcess;
import com.esteirahabitacional.financingprocess.domain.model.ProcessOrigin;
import com.esteirahabitacional.financingprocess.domain.model.ProcessPriority;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface FinancingProcessRepository {
    Optional<FinancingProcess> findById(UUID organizationId, UUID processId);
    FinancingProcess insert(FinancingProcess process);
    FinancingProcess update(FinancingProcess process, long expectedVersion);
    Page list(UUID organizationId, ProcessOrigin origin, ProcessPriority priority, int page, int size);

    record Page(List<FinancingProcess> items, long total) {}
}
