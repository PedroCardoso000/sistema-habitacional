package com.esteirahabitacional.parties.application.service;

import com.esteirahabitacional.identityaccess.AuthorizeOrganizationUseCase;
import com.esteirahabitacional.identityaccess.AuthorizeOrganizationUseCase.Action;
import com.esteirahabitacional.parties.application.port.in.AssociateBrokerAgencyUseCase;
import com.esteirahabitacional.parties.application.port.in.ChangePartnerStatusUseCase;
import com.esteirahabitacional.parties.application.port.in.RegisterAgencyUseCase;
import com.esteirahabitacional.parties.application.port.in.RegisterBrokerUseCase;
import com.esteirahabitacional.parties.application.port.out.AgencyRepository;
import com.esteirahabitacional.parties.application.port.out.BrokerRepository;
import com.esteirahabitacional.parties.application.port.out.PartyAudit;
import com.esteirahabitacional.parties.domain.model.Broker;
import com.esteirahabitacional.parties.domain.model.Cnpj;
import com.esteirahabitacional.parties.domain.model.ContactInfo;
import com.esteirahabitacional.parties.domain.model.Cpf;
import com.esteirahabitacional.parties.domain.model.PartyStatus;
import com.esteirahabitacional.parties.domain.model.RealEstateAgency;
import com.esteirahabitacional.shared.CurrentTimeProvider;
import com.esteirahabitacional.shared.IdentifierGenerator;
import java.time.Instant;

public class PartnerManagementService implements RegisterBrokerUseCase, RegisterAgencyUseCase,
        AssociateBrokerAgencyUseCase, ChangePartnerStatusUseCase {

    private final AuthorizeOrganizationUseCase authorization;
    private final BrokerRepository brokers;
    private final AgencyRepository agencies;
    private final PartyAudit audit;
    private final IdentifierGenerator identifiers;
    private final CurrentTimeProvider time;

    public PartnerManagementService(
            AuthorizeOrganizationUseCase authorization,
            BrokerRepository brokers,
            AgencyRepository agencies,
            PartyAudit audit,
            IdentifierGenerator identifiers,
            CurrentTimeProvider time) {
        this.authorization = authorization;
        this.brokers = brokers;
        this.agencies = agencies;
        this.audit = audit;
        this.identifiers = identifiers;
        this.time = time;
    }

    @Override
    public RegisterBrokerUseCase.Result execute(RegisterBrokerUseCase.Command command) {
        AuthorizeOrganizationUseCase.AuthorizedActor actor = authorize(command.organizationId());
        Cpf cpf = cpf(command.cpf());
        if (brokers.existsByCpf(command.organizationId(), cpf)) {
            throw PartyExceptions.duplicate("um corretor");
        }
        Instant occurredAt = time.now();
        Broker broker;
        try {
            broker = Broker.register(
                    identifiers.generate(), command.organizationId(), cpf, command.fullName(),
                    new ContactInfo(command.email(), command.phone()), occurredAt);
        } catch (IllegalArgumentException exception) {
            throw PartyExceptions.invalid(exception.getMessage());
        }
        brokers.save(broker);
        audit(actor.userId(), broker.organizationId(), broker.id(), "BROKER", "BROKER_REGISTERED", occurredAt);
        return new RegisterBrokerUseCase.Result(
                broker.id(), broker.fullName(), broker.contact().email(), broker.contact().phone(),
                broker.realEstateAgencyId(), broker.status());
    }

    @Override
    public RegisterAgencyUseCase.Result execute(RegisterAgencyUseCase.Command command) {
        AuthorizeOrganizationUseCase.AuthorizedActor actor = authorize(command.organizationId());
        Cnpj cnpj = cnpj(command.cnpj());
        if (agencies.existsByCnpj(command.organizationId(), cnpj)) {
            throw PartyExceptions.duplicate("uma imobiliária");
        }
        Instant occurredAt = time.now();
        RealEstateAgency agency;
        try {
            agency = RealEstateAgency.register(
                    identifiers.generate(), command.organizationId(), cnpj, command.legalName(),
                    new ContactInfo(command.email(), command.phone()), occurredAt);
        } catch (IllegalArgumentException exception) {
            throw PartyExceptions.invalid(exception.getMessage());
        }
        agencies.save(agency);
        audit(actor.userId(), agency.organizationId(), agency.id(), "AGENCY", "AGENCY_REGISTERED", occurredAt);
        return new RegisterAgencyUseCase.Result(
                agency.id(), agency.legalName(), agency.contact().email(), agency.contact().phone(), agency.status());
    }

    @Override
    public void execute(AssociateBrokerAgencyUseCase.Command command) {
        AuthorizeOrganizationUseCase.AuthorizedActor actor = authorize(command.organizationId());
        Broker broker = broker(command.organizationId(), command.brokerId());
        agencies.findById(command.organizationId(), command.agencyId())
                .orElseThrow(() -> PartyExceptions.notFound("A imobiliária"));
        Instant occurredAt = time.now();
        broker.associateWithAgency(command.agencyId(), occurredAt);
        brokers.save(broker);
        audit(actor.userId(), command.organizationId(), broker.id(),
                "BROKER", "BROKER_AGENCY_ASSOCIATED", occurredAt);
    }

    @Override
    public void execute(ChangePartnerStatusUseCase.Command command) {
        AuthorizeOrganizationUseCase.AuthorizedActor actor = authorize(command.organizationId());
        Instant occurredAt = time.now();
        if (command.type() == ChangePartnerStatusUseCase.PartnerType.BROKER) {
            Broker broker = broker(command.organizationId(), command.partnerId());
            changeStatus(command.status(), broker::activate, broker::deactivate, occurredAt);
            brokers.save(broker);
            audit(actor.userId(), command.organizationId(), broker.id(), "BROKER", "BROKER_STATUS_CHANGED", occurredAt);
        } else {
            RealEstateAgency agency = agency(command.organizationId(), command.partnerId());
            changeStatus(command.status(), agency::activate, agency::deactivate, occurredAt);
            agencies.save(agency);
            audit(actor.userId(), command.organizationId(), agency.id(), "AGENCY", "AGENCY_STATUS_CHANGED", occurredAt);
        }
    }

    private void changeStatus(
            PartyStatus status,
            java.util.function.Consumer<Instant> activate,
            java.util.function.Consumer<Instant> deactivate,
            Instant occurredAt) {
        if (status == PartyStatus.ACTIVE) {
            activate.accept(occurredAt);
        } else {
            deactivate.accept(occurredAt);
        }
    }

    private AuthorizeOrganizationUseCase.AuthorizedActor authorize(java.util.UUID organizationId) {
        return authorization.require(organizationId, Action.MANAGE_PARTIES);
    }

    private Broker broker(java.util.UUID organizationId, java.util.UUID id) {
        return brokers.findById(organizationId, id)
                .orElseThrow(() -> PartyExceptions.notFound("O corretor"));
    }

    private RealEstateAgency agency(java.util.UUID organizationId, java.util.UUID id) {
        return agencies.findById(organizationId, id)
                .orElseThrow(() -> PartyExceptions.notFound("A imobiliária"));
    }

    private Cpf cpf(String value) {
        try {
            return new Cpf(value);
        } catch (IllegalArgumentException exception) {
            throw PartyExceptions.invalid(exception.getMessage());
        }
    }

    private Cnpj cnpj(String value) {
        try {
            return new Cnpj(value);
        } catch (IllegalArgumentException exception) {
            throw PartyExceptions.invalid(exception.getMessage());
        }
    }

    private void audit(
            java.util.UUID actorId,
            java.util.UUID organizationId,
            java.util.UUID targetId,
            String targetType,
            String action,
            Instant occurredAt) {
        audit.record(new PartyAudit.Action(
                identifiers.generate(), organizationId, actorId, targetId, targetType, action, occurredAt));
    }
}
