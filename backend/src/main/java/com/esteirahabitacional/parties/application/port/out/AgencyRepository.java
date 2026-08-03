package com.esteirahabitacional.parties.application.port.out;

import com.esteirahabitacional.parties.domain.model.Cnpj;
import com.esteirahabitacional.parties.domain.model.RealEstateAgency;
import java.util.Optional;
import java.util.UUID;

public interface AgencyRepository {

    boolean existsByCnpj(UUID organizationId, Cnpj cnpj);

    Optional<RealEstateAgency> findById(UUID organizationId, UUID agencyId);

    void save(RealEstateAgency agency);
}
