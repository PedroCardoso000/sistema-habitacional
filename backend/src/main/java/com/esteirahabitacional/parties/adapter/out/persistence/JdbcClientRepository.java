package com.esteirahabitacional.parties.adapter.out.persistence;

import com.esteirahabitacional.parties.application.port.out.ClientRepository;
import com.esteirahabitacional.parties.domain.model.Client;
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

public class JdbcClientRepository implements ClientRepository {

    private final JdbcClient jdbc;

    public JdbcClientRepository(JdbcClient jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public boolean existsByCpf(UUID organizationId, Cpf cpf) {
        return jdbc.sql("SELECT EXISTS (SELECT 1 FROM party_clients "
                        + "WHERE organization_id = :organizationId AND cpf = :cpf)")
                .param("organizationId", organizationId)
                .param("cpf", cpf.value())
                .query(Boolean.class)
                .single();
    }

    @Override
    public Optional<Client> findByCpf(UUID organizationId, Cpf cpf) {
        return select("organization_id = :organizationId AND cpf = :identifier")
                .param("organizationId", organizationId)
                .param("identifier", cpf.value())
                .query(JdbcClientRepository::map)
                .optional();
    }

    @Override
    public Optional<Client> findById(UUID organizationId, UUID clientId) {
        return select("organization_id = :organizationId AND id = :identifier")
                .param("organizationId", organizationId)
                .param("identifier", clientId)
                .query(JdbcClientRepository::map)
                .optional();
    }

    @Override
    public void save(Client client) {
        try {
            jdbc.sql("INSERT INTO party_clients "
                            + "(id, organization_id, cpf, full_name, email, phone, status, created_at, updated_at) "
                            + "VALUES (:id, :organizationId, :cpf, :fullName, :email, :phone, :status, "
                            + ":createdAt, :updatedAt) ON CONFLICT (id) DO UPDATE SET email = EXCLUDED.email, "
                            + "phone = EXCLUDED.phone, status = EXCLUDED.status, updated_at = EXCLUDED.updated_at "
                            + "WHERE party_clients.organization_id = EXCLUDED.organization_id")
                    .param("id", client.id())
                    .param("organizationId", client.organizationId())
                    .param("cpf", client.cpf().value())
                    .param("fullName", client.fullName())
                    .param("email", client.contact().email())
                    .param("phone", client.contact().phone())
                    .param("status", client.status().name())
                    .param("createdAt", time(client.createdAt()))
                    .param("updatedAt", time(client.updatedAt()))
                    .update();
        } catch (DuplicateKeyException exception) {
            throw duplicate();
        }
    }

    private JdbcClient.StatementSpec select(String criteria) {
        return jdbc.sql("SELECT id, organization_id, cpf, full_name, email, phone, status, "
                + "created_at, updated_at FROM party_clients WHERE " + criteria);
    }

    private static Client map(ResultSet result, int rowNumber) throws SQLException {
        return Client.restore(
                result.getObject("id", UUID.class),
                result.getObject("organization_id", UUID.class),
                new Cpf(result.getString("cpf")),
                result.getString("full_name"),
                new ContactInfo(result.getString("email"), result.getString("phone")),
                PartyStatus.valueOf(result.getString("status")),
                result.getObject("created_at", OffsetDateTime.class).toInstant(),
                result.getObject("updated_at", OffsetDateTime.class).toInstant());
    }

    private static OffsetDateTime time(java.time.Instant instant) {
        return OffsetDateTime.ofInstant(instant, ZoneOffset.UTC);
    }

    private ApplicationException duplicate() {
        return new ApplicationException(409, "party-already-registered", "Participante já cadastrado",
                "Já existe um cliente com o identificador informado nesta empresa.");
    }
}
