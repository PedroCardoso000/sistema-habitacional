package com.esteirahabitacional.parties.config;

import com.esteirahabitacional.identityaccess.AuthorizeOrganizationUseCase;
import com.esteirahabitacional.parties.adapter.out.persistence.JdbcAgencyRepository;
import com.esteirahabitacional.parties.adapter.out.persistence.JdbcBrokerRepository;
import com.esteirahabitacional.parties.adapter.out.persistence.JdbcClientRepository;
import com.esteirahabitacional.parties.adapter.out.persistence.JdbcParticipantQuery;
import com.esteirahabitacional.parties.adapter.out.persistence.JdbcPartyAudit;
import com.esteirahabitacional.parties.application.port.in.AssociateBrokerAgencyUseCase;
import com.esteirahabitacional.parties.application.port.in.ChangePartnerStatusUseCase;
import com.esteirahabitacional.parties.application.port.in.FindClientByCpfUseCase;
import com.esteirahabitacional.parties.application.port.in.ListParticipantsUseCase;
import com.esteirahabitacional.parties.application.port.in.RegisterAgencyUseCase;
import com.esteirahabitacional.parties.application.port.in.RegisterBrokerUseCase;
import com.esteirahabitacional.parties.application.port.in.RegisterClientUseCase;
import com.esteirahabitacional.parties.application.port.in.UpdatePartyContactUseCase;
import com.esteirahabitacional.parties.application.port.out.AgencyRepository;
import com.esteirahabitacional.parties.application.port.out.BrokerRepository;
import com.esteirahabitacional.parties.application.port.out.ClientRepository;
import com.esteirahabitacional.parties.application.port.out.ParticipantQuery;
import com.esteirahabitacional.parties.application.port.out.PartyAudit;
import com.esteirahabitacional.parties.application.service.ClientManagementService;
import com.esteirahabitacional.parties.application.service.ParticipantQueryService;
import com.esteirahabitacional.parties.application.service.PartnerManagementService;
import com.esteirahabitacional.parties.application.service.PartyContactService;
import com.esteirahabitacional.parties.application.service.PartyReferenceService;
import com.esteirahabitacional.shared.CurrentTimeProvider;
import com.esteirahabitacional.shared.IdentifierGenerator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.transaction.support.TransactionTemplate;

@Configuration(proxyBeanMethods = false)
class PartiesConfiguration {

    @Bean ClientRepository clientRepository(JdbcClient jdbc) { return new JdbcClientRepository(jdbc); }
    @Bean BrokerRepository brokerRepository(JdbcClient jdbc) { return new JdbcBrokerRepository(jdbc); }
    @Bean AgencyRepository agencyRepository(JdbcClient jdbc) { return new JdbcAgencyRepository(jdbc); }
    @Bean ParticipantQuery participantQuery(JdbcClient jdbc) { return new JdbcParticipantQuery(jdbc); }
    @Bean PartyAudit partyAudit(JdbcClient jdbc) { return new JdbcPartyAudit(jdbc); }

    @Bean
    RegisterClientUseCase registerClientUseCase(
            AuthorizeOrganizationUseCase authorization,
            ClientRepository clients,
            PartyAudit audit,
            IdentifierGenerator identifiers,
            CurrentTimeProvider time,
            TransactionTemplate transactions) {
        ClientManagementService service = new ClientManagementService(
                authorization, clients, audit, identifiers, time);
        return command -> transactions.execute(status -> service.execute(command));
    }

    @Bean
    FindClientByCpfUseCase findClientByCpfUseCase(
            AuthorizeOrganizationUseCase authorization,
            ClientRepository clients,
            PartyAudit audit,
            IdentifierGenerator identifiers,
            CurrentTimeProvider time) {
        return new ClientManagementService(authorization, clients, audit, identifiers, time);
    }

    @Bean
    RegisterBrokerUseCase registerBrokerUseCase(
            AuthorizeOrganizationUseCase authorization,
            BrokerRepository brokers,
            AgencyRepository agencies,
            PartyAudit audit,
            IdentifierGenerator identifiers,
            CurrentTimeProvider time,
            TransactionTemplate transactions) {
        PartnerManagementService service = partnerService(
                authorization, brokers, agencies, audit, identifiers, time);
        return command -> transactions.execute(status -> service.execute(command));
    }

    @Bean
    RegisterAgencyUseCase registerAgencyUseCase(
            AuthorizeOrganizationUseCase authorization,
            BrokerRepository brokers,
            AgencyRepository agencies,
            PartyAudit audit,
            IdentifierGenerator identifiers,
            CurrentTimeProvider time,
            TransactionTemplate transactions) {
        PartnerManagementService service = partnerService(
                authorization, brokers, agencies, audit, identifiers, time);
        return command -> transactions.execute(status -> service.execute(command));
    }

    @Bean
    AssociateBrokerAgencyUseCase associateBrokerAgencyUseCase(
            AuthorizeOrganizationUseCase authorization,
            BrokerRepository brokers,
            AgencyRepository agencies,
            PartyAudit audit,
            IdentifierGenerator identifiers,
            CurrentTimeProvider time,
            TransactionTemplate transactions) {
        PartnerManagementService service = partnerService(
                authorization, brokers, agencies, audit, identifiers, time);
        return command -> transactions.executeWithoutResult(status -> service.execute(command));
    }

    @Bean
    ChangePartnerStatusUseCase changePartnerStatusUseCase(
            AuthorizeOrganizationUseCase authorization,
            BrokerRepository brokers,
            AgencyRepository agencies,
            PartyAudit audit,
            IdentifierGenerator identifiers,
            CurrentTimeProvider time,
            TransactionTemplate transactions) {
        PartnerManagementService service = partnerService(
                authorization, brokers, agencies, audit, identifiers, time);
        return command -> transactions.executeWithoutResult(status -> service.execute(command));
    }

    @Bean
    UpdatePartyContactUseCase updatePartyContactUseCase(
            AuthorizeOrganizationUseCase authorization,
            ClientRepository clients,
            BrokerRepository brokers,
            AgencyRepository agencies,
            PartyAudit audit,
            IdentifierGenerator identifiers,
            CurrentTimeProvider time,
            TransactionTemplate transactions) {
        PartyContactService service = new PartyContactService(
                authorization, clients, brokers, agencies, audit, identifiers, time);
        return command -> transactions.executeWithoutResult(status -> service.execute(command));
    }

    @Bean
    ListParticipantsUseCase listParticipantsUseCase(
            AuthorizeOrganizationUseCase authorization, ParticipantQuery participants) {
        return new ParticipantQueryService(authorization, participants);
    }

    @Bean
    PartyReferenceService partyReferenceService(ClientRepository clients, BrokerRepository brokers) {
        return new PartyReferenceService(clients, brokers);
    }

    private PartnerManagementService partnerService(
            AuthorizeOrganizationUseCase authorization,
            BrokerRepository brokers,
            AgencyRepository agencies,
            PartyAudit audit,
            IdentifierGenerator identifiers,
            CurrentTimeProvider time) {
        return new PartnerManagementService(authorization, brokers, agencies, audit, identifiers, time);
    }
}
