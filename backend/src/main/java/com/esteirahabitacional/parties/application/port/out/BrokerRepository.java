package com.esteirahabitacional.parties.application.port.out;

import com.esteirahabitacional.parties.domain.model.Broker;
import com.esteirahabitacional.parties.domain.model.Cpf;
import java.util.Optional;
import java.util.UUID;

public interface BrokerRepository {

    boolean existsByCpf(UUID organizationId, Cpf cpf);

    Optional<Broker> findById(UUID organizationId, UUID brokerId);

    void save(Broker broker);
}
