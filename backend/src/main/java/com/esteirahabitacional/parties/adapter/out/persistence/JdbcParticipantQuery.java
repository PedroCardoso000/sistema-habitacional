package com.esteirahabitacional.parties.adapter.out.persistence;

import com.esteirahabitacional.parties.application.port.in.ListParticipantsUseCase.ParticipantType;
import com.esteirahabitacional.parties.application.port.out.ParticipantQuery;
import com.esteirahabitacional.parties.domain.model.PartyStatus;
import java.util.List;
import java.util.UUID;
import org.springframework.jdbc.core.simple.JdbcClient;

public class JdbcParticipantQuery implements ParticipantQuery {

    private final JdbcClient jdbc;

    public JdbcParticipantQuery(JdbcClient jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public Page find(UUID organizationId, ParticipantType type, int page, int size) {
        Source source = Source.from(type);
        String criteria = " FROM " + source.table + " WHERE organization_id = :organizationId";
        List<Row> rows = jdbc.sql("SELECT id, " + source.nameColumn + " AS participant_name, status"
                        + criteria + " ORDER BY " + source.nameColumn + ", id LIMIT :limit OFFSET :offset")
                .param("organizationId", organizationId)
                .param("limit", size)
                .param("offset", page * size)
                .query((result, rowNumber) -> new Row(
                        result.getObject("id", UUID.class),
                        type,
                        result.getString("participant_name"),
                        PartyStatus.valueOf(result.getString("status"))))
                .list();
        Long total = jdbc.sql("SELECT count(*)" + criteria)
                .param("organizationId", organizationId)
                .query(Long.class)
                .single();
        return new Page(rows, total);
    }

    private record Source(String table, String nameColumn) {

        private static Source from(ParticipantType type) {
            return switch (type) {
                case CLIENT -> new Source("party_clients", "full_name");
                case BROKER -> new Source("party_brokers", "full_name");
                case AGENCY -> new Source("party_agencies", "legal_name");
            };
        }
    }
}
