package com.esteirahabitacional.parties.application.service;

import com.esteirahabitacional.parties.BrokerReferenceLookup;
import com.esteirahabitacional.parties.ClientReferenceLookup;
import com.esteirahabitacional.parties.application.port.out.BrokerRepository;
import com.esteirahabitacional.parties.application.port.out.ClientRepository;
import com.esteirahabitacional.parties.domain.model.Broker;
import com.esteirahabitacional.parties.domain.model.Client;
import java.util.UUID;

public class PartyReferenceService implements ClientReferenceLookup, BrokerReferenceLookup {

    private final ClientRepository clients;
    private final BrokerRepository brokers;

    public PartyReferenceService(ClientRepository clients, BrokerRepository brokers) {
        this.clients = clients;
        this.brokers = brokers;
    }

    @Override
    public ClientReferenceLookup.Reference find(UUID organizationId, UUID clientId) {
        Client client = clients.findById(organizationId, clientId)
                .orElseThrow(() -> PartyExceptions.notFound("O cliente"));
        return new ClientReferenceLookup.Reference(client.id(), client.organizationId(), client.fullName());
    }

    @Override
    public BrokerReferenceLookup.Reference findActive(UUID organizationId, UUID brokerId) {
        Broker broker = brokers.findById(organizationId, brokerId)
                .orElseThrow(() -> PartyExceptions.notFound("O corretor"));
        try {
            broker.ensureCanOriginateProcess();
        } catch (IllegalStateException exception) {
            throw PartyExceptions.invalid(exception.getMessage());
        }
        return new BrokerReferenceLookup.Reference(
                broker.id(), broker.organizationId(), broker.fullName(), broker.realEstateAgencyId());
    }
}
