package com.esteirahabitacional.parties.adapter.out.persistence;

import com.esteirahabitacional.parties.application.port.out.AgencyRepository;
import com.esteirahabitacional.parties.domain.model.Cnpj;
import com.esteirahabitacional.parties.domain.model.ContactInfo;
import com.esteirahabitacional.parties.domain.model.PartyStatus;
import com.esteirahabitacional.parties.domain.model.RealEstateAgency;
import com.esteirahabitacional.shared.ApplicationException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Optional;
import java.util.UUID;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.jdbc.core.simple.JdbcClient;

public class JdbcAgencyRepository implements AgencyRepository {

    private final JdbcClient jdbc;

    public JdbcAgencyRepository(JdbcClient jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public boolean existsByCnpj(UUID organizationId, Cnpj cnpj) {
        return jdbc.sql("SELECT EXISTS (SELECT 1 FROM party_agencies "
                        + "WHERE organization_id = :organizationId AND cnpj = :cnpj)")
                .param("organizationId", organizationId)
                .param("cnpj", cnpj.value())
                .query(Boolean.class)
                .single();
    }

    @Override
    public Optional<RealEstateAgency> findById(UUID organizationId, UUID agencyId) {
        return jdbc.sql("SELECT id, organization_id, cnpj, legal_name, email, phone, status, "
                        + "created_at, updated_at FROM party_agencies "
                        + "WHERE organization_id = :organizationId AND id = :agencyId")
                .param("organizationId", organizationId)
                .param("agencyId", agencyId)
                .query(JdbcAgencyRepository::map)
                .optional();
    }

    @Override
    public void save(RealEstateAgency agency) {
        try {
            jdbc.sql("INSERT INTO party_agencies "
                            + "(id, organization_id, cnpj, legal_name, email, phone, status, created_at, "
                            + "updated_at) VALUES (:id, :organizationId, :cnpj, :legalName, :email, :phone, "
                            + ":status, :createdAt, :updatedAt) ON CONFLICT (id) DO UPDATE SET "
                            + "email = EXCLUDED.email, phone = EXCLUDED.phone, status = EXCLUDED.status, "
                            + "updated_at = EXCLUDED.updated_at "
                            + "WHERE party_agencies.organization_id = EXCLUDED.organization_id")
                    .param("id", agency.id())
                    .param("organizationId", agency.organizationId())
                    .param("cnpj", agency.cnpj().value())
                    .param("legalName", agency.legalName())
                    .param("email", agency.contact().email())
                    .param("phone", agency.contact().phone())
                    .param("status", agency.status().name())
                    .param("createdAt", time(agency.createdAt()))
                    .param("updatedAt", time(agency.updatedAt()))
                    .update();
        } catch (DuplicateKeyException exception) {
            throw duplicate();
        }
    }

    private static RealEstateAgency map(ResultSet result, int rowNumber) throws SQLException {
        return RealEstateAgency.restore(
                result.getObject("id", UUID.class),
                result.getObject("organization_id", UUID.class),
                new Cnpj(result.getString("cnpj")),
                result.getString("legal_name"),
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
                "Já existe uma imobiliária com o identificador informado nesta empresa.");
    }
}
