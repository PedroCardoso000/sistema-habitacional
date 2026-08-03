package com.esteirahabitacional.financingprocess.adapter.out.persistence;

import com.esteirahabitacional.financingprocess.application.port.out.ProcessNumberGenerator;
import java.util.UUID;
import org.springframework.jdbc.core.simple.JdbcClient;

public final class JdbcProcessNumberGenerator implements ProcessNumberGenerator {
    private final JdbcClient jdbc;
    public JdbcProcessNumberGenerator(JdbcClient jdbc) { this.jdbc = jdbc; }

    @Override
    public String next(UUID organizationId) {
        Long value = jdbc.sql("INSERT INTO financing_process_number_counters (organization_id, next_value) "
                        + "VALUES (:organizationId, 1) ON CONFLICT (organization_id) DO UPDATE "
                        + "SET next_value = financing_process_number_counters.next_value + 1 RETURNING next_value")
                .param("organizationId", organizationId).query(Long.class).single();
        return "FP-" + String.format("%06d", value);
    }
}
