package com.esteirahabitacional.parties.adapter.out.persistence;

import com.esteirahabitacional.parties.application.port.out.BrokerRepository;
import com.esteirahabitacional.parties.domain.model.Broker;
import com.esteirahabitacional.parties.domain.model.ContactInfo;
import com.esteirahabitacional.parties.domain.model.Cpf;
import com.esteirahabitacional.parties.domain.model.PartyStatus;
import com.esteirahabitacional.shared.ApplicationException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Optional;
import java.util.UUID;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.jdbc.core.simple.JdbcClient;

public class JdbcBrokerRepository implements BrokerRepository {

    private final JdbcClient jdbc;

    public JdbcBrokerRepository(JdbcClient jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public boolean existsByCpf(UUID organizationId, Cpf cpf) {
        return jdbc.sql("SELECT EXISTS (SELECT 1 FROM party_brokers "
                        + "WHERE organization_id = :organizationId AND cpf = :cpf)")
                .param("organizationId", organizationId)
                .param("cpf", cpf.value())
                .query(Boolean.class)
                .single();
    }

    @Override
    public Optional<Broker> findById(UUID organizationId, UUID brokerId) {
        return jdbc.sql("SELECT id, organization_id, cpf, full_name, email, phone, agency_id, "
                        + "status, created_at, updated_at FROM party_brokers "
                        + "WHERE organization_id = :organizationId AND id = :brokerId")
                .param("organizationId", organizationId)
                .param("brokerId", brokerId)
                .query(JdbcBrokerRepository::map)
                .optional();
    }

    @Override
    public void save(Broker broker) {
        try {
            jdbc.sql("INSERT INTO party_brokers "
                            + "(id, organization_id, cpf, full_name, email, phone, agency_id, status, "
                            + "created_at, updated_at) VALUES (:id, :organizationId, :cpf, :fullName, "
                            + ":email, :phone, :agencyId, :status, :createdAt, :updatedAt) "
                            + "ON CONFLICT (id) DO UPDATE SET email = EXCLUDED.email, phone = EXCLUDED.phone, "
                            + "agency_id = EXCLUDED.agency_id, status = EXCLUDED.status, "
                            + "updated_at = EXCLUDED.updated_at "
                            + "WHERE party_brokers.organization_id = EXCLUDED.organization_id")
                    .param("id", broker.id())
                    .param("organizationId", broker.organizationId())
                    .param("cpf", broker.cpf().value())
                    .param("fullName", broker.fullName())
                    .param("email", broker.contact().email())
                    .param("phone", broker.contact().phone())
                    .param("agencyId", broker.realEstateAgencyId())
                    .param("status", broker.status().name())
                    .param("createdAt", time(broker.createdAt()))
                    .param("updatedAt", time(broker.updatedAt()))
                    .update();
        } catch (DuplicateKeyException exception) {
            throw duplicate();
        }
    }

    private static Broker map(ResultSet result, int rowNumber) throws SQLException {
        return Broker.restore(
                result.getObject("id", UUID.class),
                result.getObject("organization_id", UUID.class),
                new Cpf(result.getString("cpf")),
                result.getString("full_name"),
                new ContactInfo(result.getString("email"), result.getString("phone")),
                result.getObject("agency_id", UUID.class),
                PartyStatus.valueOf(result.getString("status")),
                result.getObject("created_at", OffsetDateTime.class).toInstant(),
                result.getObject("updated_at", OffsetDateTime.class).toInstant());
    }

    private static OffsetDateTime time(java.time.Instant instant) {
        return OffsetDateTime.ofInstant(instant, ZoneOffset.UTC);
    }

    private ApplicationException duplicate() {
        return new ApplicationException(409, "party-already-registered", "Participante já cadastrado",
                "Já existe um corretor com o identificador informado nesta empresa.");
    }
}
