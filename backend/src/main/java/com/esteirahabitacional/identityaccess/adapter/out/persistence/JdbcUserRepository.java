package com.esteirahabitacional.identityaccess.adapter.out.persistence;

import com.esteirahabitacional.identityaccess.application.port.out.UserRepository;
import com.esteirahabitacional.identityaccess.domain.model.AccessStatus;
import com.esteirahabitacional.identityaccess.domain.model.Email;
import com.esteirahabitacional.identityaccess.domain.model.Role;
import com.esteirahabitacional.identityaccess.domain.model.User;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Optional;
import java.util.UUID;
import org.slf4j.MDC;
import com.esteirahabitacional.shared.adapter.in.web.CorrelationIdFilter;
import org.springframework.jdbc.core.simple.JdbcClient;

public class JdbcUserRepository implements UserRepository {

    private final JdbcClient jdbc;

    public JdbcUserRepository(JdbcClient jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public boolean existsByEmail(UUID organizationId, Email email) {
        return jdbc.sql("SELECT EXISTS (SELECT 1 FROM identity_users "
                        + "WHERE organization_id = :organizationId AND email = :email)")
                .param("organizationId", organizationId)
                .param("email", email.value())
                .query(Boolean.class)
                .single();
    }

    @Override
    public Optional<User> findById(UUID organizationId, UUID userId) {
        return jdbc.sql("SELECT id, organization_id, email, display_name, role, status, "
                        + "created_at, access_changed_at FROM identity_users "
                        + "WHERE organization_id = :organizationId AND id = :userId")
                .param("organizationId", organizationId)
                .param("userId", userId)
                .query(JdbcUserRepository::mapUser)
                .optional();
    }

    @Override
    public void save(User user) {
        jdbc.sql("INSERT INTO identity_users "
                        + "(id, organization_id, email, display_name, role, status, created_at, access_changed_at) "
                        + "VALUES (:id, :organizationId, :email, :displayName, :role, :status, :createdAt, "
                        + ":accessChangedAt) ON CONFLICT (id) DO UPDATE SET role = EXCLUDED.role, "
                        + "status = EXCLUDED.status, access_changed_at = EXCLUDED.access_changed_at "
                        + "WHERE identity_users.organization_id = EXCLUDED.organization_id")
                .param("id", user.id())
                .param("organizationId", user.organizationId())
                .param("email", user.email().value())
                .param("displayName", user.displayName())
                .param("role", user.role().name())
                .param("status", user.status().name())
                .param("createdAt", toDatabaseTime(user.createdAt()))
                .param("accessChangedAt", toDatabaseTime(user.accessChangedAt()))
                .update();
    }

    @Override
    public void recordAccessAction(AccessAction action) {
        jdbc.sql("INSERT INTO access_action_audit "
                        + "(id, organization_id, actor_user_id, target_user_id, action, occurred_at, "
                        + "result, correlation_id, technical_origin) VALUES (:id, :organizationId, "
                        + ":actorUserId, :targetUserId, :action, :occurredAt, 'SUCCESS', :correlationId, :origin)")
                .param("id", action.id())
                .param("organizationId", action.organizationId())
                .param("actorUserId", action.actorUserId())
                .param("targetUserId", action.targetUserId())
                .param("action", action.action())
                .param("occurredAt", toDatabaseTime(action.occurredAt()))
                .param("correlationId", correlationId())
                .param("origin", action.actorUserId() == null ? "BOOTSTRAP" : "HTTP")
                .update();
    }

    private static User mapUser(ResultSet result, int rowNumber) throws SQLException {
        return User.restore(
                result.getObject("id", UUID.class),
                result.getObject("organization_id", UUID.class),
                new Email(result.getString("email")),
                result.getString("display_name"),
                Role.valueOf(result.getString("role")),
                AccessStatus.valueOf(result.getString("status")),
                result.getObject("created_at", OffsetDateTime.class).toInstant(),
                result.getObject("access_changed_at", OffsetDateTime.class).toInstant());
    }

    private static OffsetDateTime toDatabaseTime(java.time.Instant instant) {
        return OffsetDateTime.ofInstant(instant, ZoneOffset.UTC);
    }

    private static String correlationId() {
        String correlationId = MDC.get(CorrelationIdFilter.MDC_KEY);
        return correlationId == null ? "non-http" : correlationId;
    }
}
