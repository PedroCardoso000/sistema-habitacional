package com.esteirahabitacional.financingprocess.application.service;

import com.esteirahabitacional.financingprocess.application.port.in.ManageFinancingProcessUseCase.CreateCommand;
import com.esteirahabitacional.financingprocess.application.port.in.ManageFinancingProcessUseCase.MainClientCommand;
import com.esteirahabitacional.financingprocess.application.port.in.ManageFinancingProcessUseCase.ParticipantCommand;
import com.esteirahabitacional.financingprocess.application.port.in.ManageFinancingProcessUseCase.PriorityCommand;
import com.esteirahabitacional.financingprocess.application.port.in.ManageFinancingProcessUseCase.PropertyCommand;
import com.esteirahabitacional.financingprocess.application.port.in.ManageFinancingProcessUseCase.Result;
import com.esteirahabitacional.financingprocess.application.port.in.QueryFinancingProcessUseCase.Detail;
import com.esteirahabitacional.financingprocess.application.port.in.QueryFinancingProcessUseCase.ListQuery;
import com.esteirahabitacional.financingprocess.application.port.in.QueryFinancingProcessUseCase.Page;
import com.esteirahabitacional.financingprocess.application.port.in.QueryFinancingProcessUseCase.Participant;
import com.esteirahabitacional.financingprocess.application.port.in.QueryFinancingProcessUseCase.Property;
import com.esteirahabitacional.financingprocess.application.port.out.FinancingProcessRepository;
import com.esteirahabitacional.financingprocess.application.port.out.ProcessAudit;
import com.esteirahabitacional.financingprocess.application.port.out.ProcessNumberGenerator;
import com.esteirahabitacional.financingprocess.domain.event.FinancingProcessDraftCreated;
import com.esteirahabitacional.financingprocess.domain.model.FinancingProcess;
import com.esteirahabitacional.financingprocess.domain.model.ParticipantType;
import com.esteirahabitacional.financingprocess.domain.model.ProcessParticipant;
import com.esteirahabitacional.identityaccess.AuthorizeOrganizationUseCase;
import com.esteirahabitacional.parties.BrokerReferenceLookup;
import com.esteirahabitacional.parties.ClientReferenceLookup;
import com.esteirahabitacional.shared.CurrentTimeProvider;
import com.esteirahabitacional.shared.DomainEventPublisher;
import com.esteirahabitacional.shared.IdentifierGenerator;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public final class FinancingProcessService {
    private final AuthorizeOrganizationUseCase authorization;
    private final FinancingProcessRepository processes;
    private final ProcessNumberGenerator numbers;
    private final ClientReferenceLookup clients;
    private final BrokerReferenceLookup brokers;
    private final ProcessAudit audit;
    private final DomainEventPublisher events;
    private final IdentifierGenerator identifiers;
    private final CurrentTimeProvider time;

    public FinancingProcessService(AuthorizeOrganizationUseCase authorization,
            FinancingProcessRepository processes, ProcessNumberGenerator numbers,
            ClientReferenceLookup clients, BrokerReferenceLookup brokers, ProcessAudit audit,
            DomainEventPublisher events, IdentifierGenerator identifiers, CurrentTimeProvider time) {
        this.authorization = authorization;
        this.processes = processes;
        this.numbers = numbers;
        this.clients = clients;
        this.brokers = brokers;
        this.audit = audit;
        this.events = events;
        this.identifiers = identifiers;
        this.time = time;
    }

    public Result create(CreateCommand command) {
        var actor = authorization.require(command.organizationId(), AuthorizeOrganizationUseCase.Action.MANAGE_PROCESSES);
        validateOrigin(command);
        if (command.mainClientId() != null) {
            clients.find(command.organizationId(), command.mainClientId());
        }
        Instant now = time.now();
        FinancingProcess process;
        try {
            process = FinancingProcess.draft(identifiers.generate(), numbers.next(command.organizationId()),
                    command.organizationId(), command.origin(), actor.userId(), command.brokerId(),
                    command.mainClientId(), now);
        } catch (IllegalArgumentException exception) {
            throw ProcessExceptions.invalid(exception.getMessage());
        }
        process = processes.insert(process);
        audit.record(command.organizationId(), process.id(), actor.userId(), "DRAFT_CREATED", now);
        events.publish(List.of(new FinancingProcessDraftCreated(process.id(), process.organizationId(),
                process.processNumber(), process.origin(), actor.userId(), now)));
        return result(process);
    }

    public Result defineMainClient(MainClientCommand command) {
        return mutate(command.organizationId(), command.processId(), command.expectedVersion(), "MAIN_CLIENT_DEFINED",
                (process, actor, now) -> {
                    clients.find(command.organizationId(), command.clientId());
                    process.defineMainClient(command.clientId(), now);
                });
    }

    public Result associateParticipant(ParticipantCommand command) {
        return mutate(command.organizationId(), command.processId(), command.expectedVersion(), "PARTICIPANT_ASSOCIATED",
                (process, actor, now) -> {
                    if (command.type() == ParticipantType.CLIENT) {
                        clients.find(command.organizationId(), command.participantId());
                    } else {
                        brokers.findActive(command.organizationId(), command.participantId());
                    }
                    process.associateParticipant(
                            new ProcessParticipant(command.type(), command.participantId()), now);
                });
    }

    public Result associateProperty(PropertyCommand command) {
        return mutate(command.organizationId(), command.processId(), command.expectedVersion(), "PROPERTY_ASSOCIATED",
                (process, actor, now) -> process.associateProperty(command.addressLine(), command.city(),
                        command.state(), command.postalCode(), actor, now));
    }

    public Result changePriority(PriorityCommand command) {
        return mutate(command.organizationId(), command.processId(), command.expectedVersion(), "PRIORITY_CHANGED",
                (process, actor, now) -> process.changePriority(command.priority(), now));
    }

    public Detail find(UUID organizationId, UUID processId) {
        authorization.require(organizationId, AuthorizeOrganizationUseCase.Action.VIEW_PROCESSES);
        return detail(load(organizationId, processId));
    }

    public Page list(ListQuery query) {
        authorization.require(query.organizationId(), AuthorizeOrganizationUseCase.Action.VIEW_PROCESSES);
        if (query.page() < 0 || query.size() < 1 || query.size() > 100) {
            throw ProcessExceptions.invalid("Paginação inválida.");
        }
        var page = processes.list(query.organizationId(), query.origin(), query.priority(), query.page(), query.size());
        return new Page(page.items().stream().map(FinancingProcessService::detail).toList(),
                query.page(), query.size(), page.total());
    }

    private Result mutate(UUID organizationId, UUID processId, long expectedVersion, String action,
            Mutation mutation) {
        var actor = authorization.require(organizationId, AuthorizeOrganizationUseCase.Action.MANAGE_PROCESSES);
        FinancingProcess process = load(organizationId, processId);
        if (process.version() != expectedVersion) {
            throw ProcessExceptions.conflict();
        }
        Instant now = time.now();
        try {
            mutation.apply(process, actor.userId(), now);
        } catch (IllegalArgumentException | IllegalStateException exception) {
            throw ProcessExceptions.invalid(exception.getMessage());
        }
        process = processes.update(process, expectedVersion);
        audit.record(organizationId, processId, actor.userId(), action, now);
        return result(process);
    }

    private void validateOrigin(CreateCommand command) {
        if (command.origin() == null) {
            throw ProcessExceptions.invalid("A origem é obrigatória.");
        }
        if (command.brokerId() != null) {
            brokers.findActive(command.organizationId(), command.brokerId());
        }
    }

    private FinancingProcess load(UUID organizationId, UUID processId) {
        return processes.findById(organizationId, processId).orElseThrow(ProcessExceptions::notFound);
    }

    private static Result result(FinancingProcess process) {
        return new Result(process.id(), process.processNumber(), process.version());
    }

    private static Detail detail(FinancingProcess process) {
        return new Detail(process.id(), process.processNumber(), process.organizationId(), process.origin(),
                process.status(),
                process.authorUserId(), process.brokerId(), process.responsibleUserId(), process.mainClientId(),
                process.priority(), process.participants().stream()
                        .map(item -> new Participant(item.type(), item.participantId())).toList(),
                process.propertyHistory().stream().map(item -> new Property(item.sequence(), item.addressLine(),
                        item.city(), item.state(), item.postalCode(), item.associatedBy(), item.associatedAt())).toList(),
                process.version(), process.createdAt(), process.updatedAt());
    }

    private interface Mutation {
        void apply(FinancingProcess process, UUID actorId, Instant now);
    }
}
