package com.esteirahabitacional.documents.adapter.out.persistence;

import com.esteirahabitacional.documents.application.port.out.DocumentAudit;
import com.esteirahabitacional.shared.IdentifierGenerator;
import com.esteirahabitacional.shared.adapter.in.web.CorrelationIdFilter;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.UUID;
import org.slf4j.MDC;
import org.springframework.jdbc.core.simple.JdbcClient;

public final class JdbcDocumentAudit implements DocumentAudit {
    private final JdbcClient jdbc;
    private final IdentifierGenerator identifiers;
    public JdbcDocumentAudit(JdbcClient jdbc, IdentifierGenerator identifiers) {
        this.jdbc = jdbc;
        this.identifiers = identifiers;
    }
    @Override
    public void record(UUID organizationId, UUID processId, UUID actorId, String action, Instant occurredAt) {
        jdbc.sql("INSERT INTO document_audit (id, organization_id, process_id, actor_id, action, result, "
                        + "correlation_id, occurred_at) VALUES (:id, :organizationId, :processId, :actorId, "
                        + ":action, 'SUCCESS', :correlationId, :occurredAt)")
                .param("id", identifiers.generate()).param("organizationId", organizationId)
                .param("processId", processId).param("actorId", actorId).param("action", action)
                .param("correlationId", MDC.get(CorrelationIdFilter.MDC_KEY))
                .param("occurredAt", OffsetDateTime.ofInstant(occurredAt, ZoneOffset.UTC)).update();
    }
}
