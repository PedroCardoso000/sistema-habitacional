package com.esteirahabitacional.workflow.adapter.out.persistence;

import com.esteirahabitacional.workflow.application.port.out.WorkflowAudit;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.UUID;
import org.slf4j.MDC;
import org.springframework.jdbc.core.simple.JdbcClient;

public final class JdbcWorkflowAudit implements WorkflowAudit {
    private final JdbcClient jdbc;
    public JdbcWorkflowAudit(JdbcClient jdbc) { this.jdbc = jdbc; }

    @Override
    public void record(UUID organizationId, UUID processId, UUID actorId, String action, Instant occurredAt) {
        jdbc.sql("INSERT INTO workflow_audit (id, organization_id, process_id, actor_id, action, result, "
                        + "correlation_id, occurred_at) VALUES (:id, :organizationId, :processId, :actorId, "
                        + ":action, 'SUCCESS', :correlationId, :occurredAt)")
                .param("id", UUID.randomUUID()).param("organizationId", organizationId)
                .param("processId", processId).param("actorId", actorId).param("action", action)
                .param("correlationId", MDC.get("correlationId"))
                .param("occurredAt", OffsetDateTime.ofInstant(occurredAt, ZoneOffset.UTC)).update();
    }
}
