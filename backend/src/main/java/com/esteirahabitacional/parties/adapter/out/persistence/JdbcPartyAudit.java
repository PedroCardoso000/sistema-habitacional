package com.esteirahabitacional.parties.adapter.out.persistence;

import com.esteirahabitacional.parties.application.port.out.PartyAudit;
import com.esteirahabitacional.shared.adapter.in.web.CorrelationIdFilter;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import org.slf4j.MDC;
import org.springframework.jdbc.core.simple.JdbcClient;

public class JdbcPartyAudit implements PartyAudit {

    private final JdbcClient jdbc;

    public JdbcPartyAudit(JdbcClient jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public void record(Action action) {
        jdbc.sql("INSERT INTO party_action_audit "
                        + "(id, organization_id, actor_user_id, target_id, target_type, action, occurred_at, "
                        + "result, correlation_id, technical_origin) VALUES (:id, :organizationId, "
                        + ":actorUserId, :targetId, :targetType, :action, :occurredAt, 'SUCCESS', "
                        + ":correlationId, 'HTTP')")
                .param("id", action.id())
                .param("organizationId", action.organizationId())
                .param("actorUserId", action.actorUserId())
                .param("targetId", action.targetId())
                .param("targetType", action.targetType())
                .param("action", action.action())
                .param("occurredAt", OffsetDateTime.ofInstant(action.occurredAt(), ZoneOffset.UTC))
                .param("correlationId", correlationId())
                .update();
    }

    private String correlationId() {
        String correlationId = MDC.get(CorrelationIdFilter.MDC_KEY);
        return correlationId == null ? "non-http" : correlationId;
    }
}
