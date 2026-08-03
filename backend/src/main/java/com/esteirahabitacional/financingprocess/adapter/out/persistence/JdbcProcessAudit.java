package com.esteirahabitacional.financingprocess.adapter.out.persistence;

import com.esteirahabitacional.financingprocess.application.port.out.ProcessAudit;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.UUID;
import org.slf4j.MDC;
import org.springframework.jdbc.core.simple.JdbcClient;

public final class JdbcProcessAudit implements ProcessAudit {
    private final JdbcClient jdbc;
    public JdbcProcessAudit(JdbcClient jdbc) { this.jdbc = jdbc; }
    @Override
    public void record(UUID organizationId, UUID processId, UUID actorId, String action, Instant occurredAt) {
        jdbc.sql("INSERT INTO financing_process_audit "
                        + "(id, organization_id, process_id, actor_id, action, correlation_id, occurred_at) "
                        + "VALUES (:id, :organizationId, :processId, :actorId, :action, :correlationId, :occurredAt)")
                .param("id", UUID.randomUUID()).param("organizationId", organizationId)
                .param("processId", processId).param("actorId", actorId).param("action", action)
                .param("correlationId", MDC.get("correlationId"))
                .param("occurredAt", OffsetDateTime.ofInstant(occurredAt, ZoneOffset.UTC)).update();
    }
}
