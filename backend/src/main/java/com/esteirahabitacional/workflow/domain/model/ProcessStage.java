package com.esteirahabitacional.workflow.domain.model;

import java.time.Instant;

public record ProcessStage(
        String code, String name, int position, StageStatus status,
        Instant startedAt, Instant completedAt) {

    public ProcessStage withStatus(StageStatus newStatus, Instant occurredAt) {
        Instant start = startedAt;
        Instant completion = completedAt;
        if ((newStatus == StageStatus.CURRENT || newStatus == StageStatus.BLOCKED) && start == null) {
            start = occurredAt;
        }
        if (newStatus == StageStatus.COMPLETED && completion == null) {
            completion = occurredAt;
        } else if (newStatus == StageStatus.PENDING || newStatus == StageStatus.CURRENT) {
            completion = null;
        }
        return new ProcessStage(code, name, position, newStatus, start, completion);
    }
}
