package com.esteirahabitacional.parties.application.service;

import com.esteirahabitacional.identityaccess.AuthorizeOrganizationUseCase;
import com.esteirahabitacional.identityaccess.AuthorizeOrganizationUseCase.Action;
import com.esteirahabitacional.parties.application.port.in.FindClientByCpfUseCase;
import com.esteirahabitacional.parties.application.port.in.RegisterClientUseCase;
import com.esteirahabitacional.parties.application.port.out.ClientRepository;
import com.esteirahabitacional.parties.application.port.out.PartyAudit;
import com.esteirahabitacional.parties.domain.model.Client;
import com.esteirahabitacional.parties.domain.model.ContactInfo;
import com.esteirahabitacional.parties.domain.model.Cpf;
import com.esteirahabitacional.shared.CurrentTimeProvider;
import com.esteirahabitacional.shared.IdentifierGenerator;
import java.time.Instant;

public class ClientManagementService implements RegisterClientUseCase, FindClientByCpfUseCase {

    private final AuthorizeOrganizationUseCase authorization;
    private final ClientRepository clients;
    private final PartyAudit audit;
    private final IdentifierGenerator identifiers;
    private final CurrentTimeProvider time;

    public ClientManagementService(
            AuthorizeOrganizationUseCase authorization,
            ClientRepository clients,
            PartyAudit audit,
            IdentifierGenerator identifiers,
            CurrentTimeProvider time) {
        this.authorization = authorization;
        this.clients = clients;
        this.audit = audit;
        this.identifiers = identifiers;
        this.time = time;
    }

    @Override
    public RegisterClientUseCase.Result execute(RegisterClientUseCase.Command command) {
        AuthorizeOrganizationUseCase.AuthorizedActor actor =
                authorization.require(command.organizationId(), Action.MANAGE_PARTIES);
        Cpf cpf = cpf(command.cpf());
        if (clients.existsByCpf(command.organizationId(), cpf)) {
            throw PartyExceptions.duplicate("um cliente");
        }
        Instant occurredAt = time.now();
        Client client;
        try {
            client = Client.register(
                    identifiers.generate(), command.organizationId(), cpf, command.fullName(),
                    new ContactInfo(command.email(), command.phone()), occurredAt);
        } catch (IllegalArgumentException exception) {
            throw PartyExceptions.invalid(exception.getMessage());
        }
        clients.save(client);
        audit(actor.userId(), client, "CLIENT_REGISTERED", occurredAt);
        return result(client);
    }

    @Override
    public FindClientByCpfUseCase.Result execute(FindClientByCpfUseCase.Query query) {
        authorization.require(query.organizationId(), Action.VIEW_PARTIES);
        Client client = clients.findByCpf(query.organizationId(), cpf(query.cpf()))
                .orElseThrow(() -> PartyExceptions.notFound("O cliente"));
        return new FindClientByCpfUseCase.Result(
                client.id(), client.fullName(), client.contact().email(), client.contact().phone(), client.status());
    }

    private Cpf cpf(String value) {
        try {
            return new Cpf(value);
        } catch (IllegalArgumentException exception) {
            throw PartyExceptions.invalid(exception.getMessage());
        }
    }

    private RegisterClientUseCase.Result result(Client client) {
        return new RegisterClientUseCase.Result(
                client.id(), client.fullName(), client.contact().email(), client.contact().phone(), client.status());
    }

    private void audit(java.util.UUID actorId, Client client, String action, Instant occurredAt) {
        audit.record(new PartyAudit.Action(
                identifiers.generate(), client.organizationId(), actorId, client.id(), "CLIENT", action, occurredAt));
    }
}
