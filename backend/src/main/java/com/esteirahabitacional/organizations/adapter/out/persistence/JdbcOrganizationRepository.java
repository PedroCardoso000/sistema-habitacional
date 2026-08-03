package com.esteirahabitacional.organizations.adapter.out.persistence;

import com.esteirahabitacional.organizations.application.port.out.OrganizationRepository;
import com.esteirahabitacional.organizations.domain.model.Organization;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import org.springframework.jdbc.core.simple.JdbcClient;

public class JdbcOrganizationRepository implements OrganizationRepository {

    private static final long BOOTSTRAP_LOCK_ID = 4_517_001L;
    private final JdbcClient jdbc;

    public JdbcOrganizationRepository(JdbcClient jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public void acquireBootstrapLock() {
        jdbc.sql("SELECT pg_advisory_xact_lock(:lockId)")
                .param("lockId", BOOTSTRAP_LOCK_ID)
                .query()
                .singleValue();
    }

    @Override
    public boolean existsAny() {
        return jdbc.sql("SELECT EXISTS (SELECT 1 FROM organizations)")
                .query(Boolean.class)
                .single();
    }

    @Override
    public void save(Organization organization) {
        jdbc.sql("INSERT INTO organizations (id, name, created_at) VALUES (:id, :name, :createdAt)")
                .param("id", organization.id())
                .param("name", organization.name())
                .param("createdAt", OffsetDateTime.ofInstant(organization.createdAt(), ZoneOffset.UTC))
                .update();
    }
}
