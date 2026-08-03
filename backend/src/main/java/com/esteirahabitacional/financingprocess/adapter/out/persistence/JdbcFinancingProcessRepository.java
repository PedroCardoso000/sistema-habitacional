package com.esteirahabitacional.financingprocess.adapter.out.persistence;

import com.esteirahabitacional.financingprocess.application.port.out.FinancingProcessRepository;
import com.esteirahabitacional.financingprocess.domain.model.FinancingProcess;
import com.esteirahabitacional.financingprocess.domain.model.ParticipantType;
import com.esteirahabitacional.financingprocess.domain.model.ProcessOrigin;
import com.esteirahabitacional.financingprocess.domain.model.ProcessParticipant;
import com.esteirahabitacional.financingprocess.domain.model.ProcessPriority;
import com.esteirahabitacional.financingprocess.domain.model.PropertyAssociation;
import com.esteirahabitacional.shared.ApplicationException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import org.springframework.jdbc.core.simple.JdbcClient;

public final class JdbcFinancingProcessRepository implements FinancingProcessRepository {
    private final JdbcClient jdbc;
    public JdbcFinancingProcessRepository(JdbcClient jdbc) { this.jdbc = jdbc; }

    @Override
    public Optional<FinancingProcess> findById(UUID organizationId, UUID processId) {
        return jdbc.sql(baseSelect() + " WHERE organization_id = :organizationId AND id = :processId")
                .param("organizationId", organizationId).param("processId", processId)
                .query((result, row) -> map(result)).optional();
    }

    @Override
    public FinancingProcess insert(FinancingProcess process) {
        jdbc.sql("INSERT INTO financing_processes (id, organization_id, process_number, origin, author_user_id, "
                        + "broker_id, responsible_user_id, main_client_id, priority, version, created_at, updated_at) "
                        + "VALUES (:id, :organizationId, :number, :origin, :author, :broker, :responsible, "
                        + ":client, :priority, 0, :createdAt, :updatedAt)")
                .param("id", process.id()).param("organizationId", process.organizationId())
                .param("number", process.processNumber()).param("origin", process.origin().name())
                .param("author", process.authorUserId()).param("broker", process.brokerId())
                .param("responsible", process.responsibleUserId()).param("client", process.mainClientId())
                .param("priority", process.priority().name()).param("createdAt", time(process.createdAt()))
                .param("updatedAt", time(process.updatedAt())).update();
        persistChildren(process);
        return process;
    }

    @Override
    public FinancingProcess update(FinancingProcess process, long expectedVersion) {
        int changed = jdbc.sql("UPDATE financing_processes SET main_client_id = :client, priority = :priority, "
                        + "version = version + 1, updated_at = :updatedAt WHERE organization_id = :organizationId "
                        + "AND id = :id AND version = :expectedVersion")
                .param("client", process.mainClientId()).param("priority", process.priority().name())
                .param("updatedAt", time(process.updatedAt())).param("organizationId", process.organizationId())
                .param("id", process.id()).param("expectedVersion", expectedVersion).update();
        if (changed == 0) {
            throw conflict();
        }
        persistChildren(process);
        process.persistedAtVersion(expectedVersion + 1);
        return process;
    }

    @Override
    public Page list(UUID organizationId, ProcessOrigin origin, ProcessPriority priority, int page, int size) {
        String filter = " WHERE organization_id = :organizationId"
                + (origin == null ? "" : " AND origin = :origin")
                + (priority == null ? "" : " AND priority = :priority");
        JdbcClient.StatementSpec query = jdbc.sql(baseSelect() + filter
                        + " ORDER BY created_at DESC, id LIMIT :size OFFSET :offset")
                .param("organizationId", organizationId).param("size", size).param("offset", page * size);
        JdbcClient.StatementSpec count = jdbc.sql("SELECT count(*) FROM financing_processes" + filter)
                .param("organizationId", organizationId);
        if (origin != null) {
            query = query.param("origin", origin.name());
            count = count.param("origin", origin.name());
        }
        if (priority != null) {
            query = query.param("priority", priority.name());
            count = count.param("priority", priority.name());
        }
        return new Page(query.query((result, row) -> map(result)).list(), count.query(Long.class).single());
    }

    private FinancingProcess map(ResultSet result) throws SQLException {
        UUID id = result.getObject("id", UUID.class);
        UUID organizationId = result.getObject("organization_id", UUID.class);
        Set<ProcessParticipant> participants = new LinkedHashSet<>(jdbc.sql(
                        "SELECT participant_type, participant_id FROM financing_process_participants "
                                + "WHERE organization_id = :organizationId AND process_id = :processId")
                .param("organizationId", organizationId).param("processId", id)
                .query((row, number) -> new ProcessParticipant(ParticipantType.valueOf(row.getString(1)),
                        row.getObject(2, UUID.class))).list());
        List<PropertyAssociation> history = jdbc.sql("SELECT sequence, address_line, city, state, postal_code, "
                        + "associated_by, associated_at FROM financing_process_property_history "
                        + "WHERE organization_id = :organizationId AND process_id = :processId ORDER BY sequence")
                .param("organizationId", organizationId).param("processId", id)
                .query((row, number) -> new PropertyAssociation(row.getInt("sequence"),
                        row.getString("address_line"), row.getString("city"), row.getString("state"),
                        row.getString("postal_code"), row.getObject("associated_by", UUID.class),
                        row.getObject("associated_at", OffsetDateTime.class).toInstant())).list();
        return FinancingProcess.restore(id, result.getString("process_number"), organizationId,
                ProcessOrigin.valueOf(result.getString("origin")), result.getObject("author_user_id", UUID.class),
                result.getObject("broker_id", UUID.class), result.getObject("responsible_user_id", UUID.class),
                result.getObject("main_client_id", UUID.class), ProcessPriority.valueOf(result.getString("priority")),
                participants, history, result.getLong("version"),
                result.getObject("created_at", OffsetDateTime.class).toInstant(),
                result.getObject("updated_at", OffsetDateTime.class).toInstant());
    }

    private void persistChildren(FinancingProcess process) {
        process.participants().forEach(participant -> jdbc.sql("INSERT INTO financing_process_participants "
                        + "(organization_id, process_id, participant_type, participant_id) "
                        + "VALUES (:organizationId, :processId, :type, :participantId) ON CONFLICT DO NOTHING")
                .param("organizationId", process.organizationId()).param("processId", process.id())
                .param("type", participant.type().name()).param("participantId", participant.participantId()).update());
        process.propertyHistory().forEach(property -> jdbc.sql("INSERT INTO financing_process_property_history "
                        + "(organization_id, process_id, sequence, address_line, city, state, postal_code, "
                        + "associated_by, associated_at) VALUES (:organizationId, :processId, :sequence, "
                        + ":addressLine, :city, :state, :postalCode, :associatedBy, :associatedAt) ON CONFLICT DO NOTHING")
                .param("organizationId", process.organizationId()).param("processId", process.id())
                .param("sequence", property.sequence()).param("addressLine", property.addressLine())
                .param("city", property.city()).param("state", property.state())
                .param("postalCode", property.postalCode()).param("associatedBy", property.associatedBy())
                .param("associatedAt", time(property.associatedAt())).update());
    }

    private String baseSelect() {
        return "SELECT id, organization_id, process_number, origin, author_user_id, broker_id, "
                + "responsible_user_id, main_client_id, priority, version, created_at, updated_at "
                + "FROM financing_processes";
    }
    private static OffsetDateTime time(java.time.Instant instant) {
        return OffsetDateTime.ofInstant(instant, ZoneOffset.UTC);
    }
    private ApplicationException conflict() {
        return new ApplicationException(409, "process-version-conflict", "Conflito de versão",
                "O processo foi alterado por outra operação. Recarregue os dados e tente novamente.");
    }
}
