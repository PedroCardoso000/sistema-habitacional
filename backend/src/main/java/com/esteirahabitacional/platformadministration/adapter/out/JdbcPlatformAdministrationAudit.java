package com.esteirahabitacional.platformadministration.adapter.out;

import com.esteirahabitacional.platformadministration.application.port.out.PlatformAdministrationAudit;
import com.esteirahabitacional.shared.adapter.in.web.CorrelationIdFilter;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.UUID;
import org.slf4j.MDC;
import org.springframework.jdbc.core.simple.JdbcClient;

public class JdbcPlatformAdministrationAudit implements PlatformAdministrationAudit {

    private final JdbcClient jdbc;

    public JdbcPlatformAdministrationAudit(JdbcClient jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public void recordOrganizationCreated(
            UUID actionId,
            UUID actorOrganizationId,
            UUID actorUserId,
            UUID createdOrganizationId,
            Instant occurredAt) {
        jdbc.sql("INSERT INTO platform_administration_audit "
                        + "(id, actor_organization_id, actor_user_id, target_organization_id, action, "
                        + "occurred_at, result, correlation_id) VALUES (:id, :actorOrganizationId, "
                        + ":actorUserId, :targetOrganizationId, 'ORGANIZATION_CREATED', :occurredAt, "
                        + "'SUCCESS', :correlationId)")
                .param("id", actionId)
                .param("actorOrganizationId", actorOrganizationId)
                .param("actorUserId", actorUserId)
                .param("targetOrganizationId", createdOrganizationId)
                .param("occurredAt", OffsetDateTime.ofInstant(occurredAt, ZoneOffset.UTC))
                .param("correlationId", correlationId())
                .update();
    }

    private String correlationId() {
        String correlationId = MDC.get(CorrelationIdFilter.MDC_KEY);
        return correlationId == null ? "non-http" : correlationId;
    }
}
