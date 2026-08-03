package com.esteirahabitacional.parties.application.service;

import com.esteirahabitacional.identityaccess.AuthorizeOrganizationUseCase;
import com.esteirahabitacional.identityaccess.AuthorizeOrganizationUseCase.Action;
import com.esteirahabitacional.parties.application.port.in.UpdatePartyContactUseCase;
import com.esteirahabitacional.parties.application.port.out.AgencyRepository;
import com.esteirahabitacional.parties.application.port.out.BrokerRepository;
import com.esteirahabitacional.parties.application.port.out.ClientRepository;
import com.esteirahabitacional.parties.application.port.out.PartyAudit;
import com.esteirahabitacional.parties.domain.model.Broker;
import com.esteirahabitacional.parties.domain.model.Client;
import com.esteirahabitacional.parties.domain.model.ContactInfo;
import com.esteirahabitacional.parties.domain.model.RealEstateAgency;
import com.esteirahabitacional.shared.CurrentTimeProvider;
import com.esteirahabitacional.shared.IdentifierGenerator;
import java.time.Instant;

public class PartyContactService implements UpdatePartyContactUseCase {

    private final AuthorizeOrganizationUseCase authorization;
    private final ClientRepository clients;
    private final BrokerRepository brokers;
    private final AgencyRepository agencies;
    private final PartyAudit audit;
    private final IdentifierGenerator identifiers;
    private final CurrentTimeProvider time;

    public PartyContactService(
            AuthorizeOrganizationUseCase authorization,
            ClientRepository clients,
            BrokerRepository brokers,
            AgencyRepository agencies,
            PartyAudit audit,
            IdentifierGenerator identifiers,
            CurrentTimeProvider time) {
        this.authorization = authorization;
        this.clients = clients;
        this.brokers = brokers;
        this.agencies = agencies;
        this.audit = audit;
        this.identifiers = identifiers;
        this.time = time;
    }

    @Override
    public void execute(Command command) {
        AuthorizeOrganizationUseCase.AuthorizedActor actor =
                authorization.require(command.organizationId(), Action.MANAGE_PARTIES);
        ContactInfo contact;
        try {
            contact = new ContactInfo(command.email(), command.phone());
        } catch (IllegalArgumentException exception) {
            throw PartyExceptions.invalid(exception.getMessage());
        }
        Instant occurredAt = time.now();
        switch (command.type()) {
            case CLIENT -> updateClient(command, contact, actor.userId(), occurredAt);
            case BROKER -> updateBroker(command, contact, actor.userId(), occurredAt);
            case AGENCY -> updateAgency(command, contact, actor.userId(), occurredAt);
        }
    }

    private void updateClient(Command command, ContactInfo contact, java.util.UUID actorId, Instant occurredAt) {
        Client client = clients.findById(command.organizationId(), command.partyId())
                .orElseThrow(() -> PartyExceptions.notFound("O cliente"));
        client.updateContact(contact, occurredAt);
        clients.save(client);
        audit(actorId, command, "CLIENT", occurredAt);
    }

    private void updateBroker(Command command, ContactInfo contact, java.util.UUID actorId, Instant occurredAt) {
        Broker broker = brokers.findById(command.organizationId(), command.partyId())
                .orElseThrow(() -> PartyExceptions.notFound("O corretor"));
        broker.updateContact(contact, occurredAt);
        brokers.save(broker);
        audit(actorId, command, "BROKER", occurredAt);
    }

    private void updateAgency(Command command, ContactInfo contact, java.util.UUID actorId, Instant occurredAt) {
        RealEstateAgency agency = agencies.findById(command.organizationId(), command.partyId())
                .orElseThrow(() -> PartyExceptions.notFound("A imobiliária"));
        agency.updateContact(contact, occurredAt);
        agencies.save(agency);
        audit(actorId, command, "AGENCY", occurredAt);
    }

    private void audit(java.util.UUID actorId, Command command, String type, Instant occurredAt) {
        audit.record(new PartyAudit.Action(
                identifiers.generate(), command.organizationId(), actorId, command.partyId(),
                type, "CONTACT_UPDATED", occurredAt));
    }
}
