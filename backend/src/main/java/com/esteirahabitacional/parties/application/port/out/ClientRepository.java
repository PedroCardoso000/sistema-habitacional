package com.esteirahabitacional.parties.application.port.out;

import com.esteirahabitacional.parties.domain.model.Client;
import com.esteirahabitacional.parties.domain.model.Cpf;
import java.util.Optional;
import java.util.UUID;

public interface ClientRepository {

    boolean existsByCpf(UUID organizationId, Cpf cpf);

    Optional<Client> findByCpf(UUID organizationId, Cpf cpf);

    Optional<Client> findById(UUID organizationId, UUID clientId);

    void save(Client client);
}
